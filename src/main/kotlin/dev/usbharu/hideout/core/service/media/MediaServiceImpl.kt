package dev.usbharu.hideout.core.service.media

import dev.usbharu.hideout.core.domain.exception.media.MediaSaveException
import dev.usbharu.hideout.core.domain.exception.media.UnsupportedMediaException
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.service.media.converter.MediaProcessService
import dev.usbharu.hideout.mastodon.interfaces.api.media.MediaRequest
import dev.usbharu.hideout.util.withDelete
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.util.*
import javax.imageio.ImageIO
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia

@Service
@Suppress("TooGenericExceptionCaught")
class MediaServiceImpl(
    private val mediaDataStore: MediaDataStore,
    private val fileTypeDeterminationService: FileTypeDeterminationService,
    private val mediaBlurhashService: MediaBlurhashService,
    private val mediaRepository: MediaRepository,
    private val mediaProcessServices: List<MediaProcessService>,
    private val httpClient: HttpClient
) : MediaService {
    override suspend fun uploadLocalMedia(mediaRequest: MediaRequest): EntityMedia {
        val fileName = mediaRequest.file.name
        logger.info(
            "Media upload. filename:$fileName " +
                    "contentType:${mediaRequest.file.contentType}"
        )

        val tempFile = Files.createTempFile("hideout-tmp-file", ".tmp")



        tempFile.withDelete().use {
            Files.newOutputStream(tempFile).use { outputStream ->
                mediaRequest.file.inputStream.use {
                    it.transferTo(outputStream)
                }
            }
            val mimeType = fileTypeDeterminationService.fileType(tempFile, fileName)

            val process = findMediaProcessor(mimeType).process(
                mimeType,
                fileName,
                tempFile,
                null
            )

            val dataMediaSave = MediaSaveRequest(
                process.filePath.fileName.toString(),
                "",
                process.filePath,
                process.thumbnailPath
            )
            dataMediaSave.filePath.withDelete().use {
                dataMediaSave.thumbnailPath.withDelete().use {
                    val save = try {
                        mediaDataStore.save(dataMediaSave)
                    } catch (e: Exception) {
                        logger.warn("Failed to save the media", e)
                        throw MediaSaveException("Failed to save the media.", e)
                    }
                    if (save.success.not()) {
                        save as FaildSavedMedia
                        logger.warn("Failed to save the media. reason: ${save.reason}")
                        logger.warn(save.description, save.trace)
                        throw MediaSaveException("Failed to save the media.")
                    }
                    save as SuccessSavedMedia
                    val blurHash = generateBlurhash(process)
                    return mediaRepository.save(
                        EntityMedia(
                            id = mediaRepository.generateId(),
                            name = fileName,
                            url = save.url,
                            remoteUrl = null,
                            thumbnailUrl = save.thumbnailUrl,
                            type = process.fileMimeType.fileType,
                            mimeType = process.fileMimeType,
                            blurHash = blurHash
                        )
                    )
                }
            }
        }


    }

    // TODO: 仮の処理として保存したように動かす
    override suspend fun uploadRemoteMedia(remoteMedia: RemoteMedia): Media {
        logger.info("MEDIA Remote media. filename:${remoteMedia.name} url:${remoteMedia.url}")

        val httpResponse = httpClient.get(remoteMedia.url)
        val bytes = httpResponse.bodyAsChannel().toByteArray()

        val contentType = httpResponse.contentType()?.toString()
        val mimeType =
            fileTypeDeterminationService.fileType(bytes, remoteMedia.name, contentType)

        if (mimeType.fileType != FileType.Image) {
            throw UnsupportedMediaException("FileType: $mimeType isn't supported.")
        }

        val processedMedia = mediaProcessServices.first().process(
            fileType = mimeType.fileType,
            contentType = contentType.orEmpty(),
            fileName = remoteMedia.name,
            file = bytes,
            thumbnail = null
        )

        val mediaSave = MediaSave(
            "${UUID.randomUUID()}.${processedMedia.file.extension}",
            "",
            processedMedia.file.byteArray,
            processedMedia.thumbnail?.byteArray
        )

        val save = try {
            mediaDataStore.save(mediaSave)
        } catch (e: Exception) {
            logger.warn("Failed save media", e)
            throw MediaSaveException("Failed save media.", e)
        }

        if (save.success.not()) {
            save as FaildSavedMedia
            logger.warn("Failed to save the media. reason: ${save.reason}")
            logger.warn(save.description, save.trace)
            throw MediaSaveException("Failed to save the media.")
        }
        save as SuccessSavedMedia

        val blurhash = withContext(Dispatchers.IO) {
            mediaBlurhashService.generateBlurhash(ImageIO.read(bytes.inputStream()))
        }

        return mediaRepository.save(
            EntityMedia(
                id = mediaRepository.generateId(),
                name = remoteMedia.name,
                url = save.url,
                remoteUrl = remoteMedia.url,
                thumbnailUrl = save.thumbnailUrl,
                type = mimeType.fileType,
                mimeType = mimeType,
                blurHash = blurhash
            )
        )
    }

    protected fun findMediaProcessor(mimeType: MimeType): MediaProcessService {
        try {
            return mediaProcessServices.first {
                try {
                    it.isSupport(mimeType)
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: NoSuchElementException) {
            throw UnsupportedMediaException("MediaType: $mimeType isn't supported.")
        }
    }

    protected fun generateBlurhash(process: ProcessedMediaPath): String {
        val path = if (process.thumbnailPath != null && process.thumbnailMimeType != null) {
            process.thumbnailPath
        } else {
            process.filePath
        }
        val mimeType = if (process.thumbnailPath != null && process.thumbnailMimeType != null) {
            process.thumbnailMimeType
        } else {
            process.fileMimeType
        }

        val imageReadersByMIMEType = ImageIO.getImageReadersByMIMEType(mimeType.type + "/" + mimeType.subtype)
        for (imageReader in imageReadersByMIMEType) {
            try {
                val bufferedImage = ImageIO.createImageInputStream(path.toFile()).use {
                    imageReader.input = it
                    imageReader.read(0)
                }
                return mediaBlurhashService.generateBlurhash(bufferedImage)
            } catch (e: Exception) {
                logger.warn("Failed to read thumbnail", e)
            }
        }
        return ""
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MediaServiceImpl::class.java)
    }
}
