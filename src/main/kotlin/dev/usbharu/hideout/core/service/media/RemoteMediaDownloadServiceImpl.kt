package dev.usbharu.hideout.core.service.media

import dev.usbharu.hideout.core.service.resource.KtorResourceResolveService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.outputStream

@Service
class RemoteMediaDownloadServiceImpl(private val resourceResolveService: KtorResourceResolveService) :
    RemoteMediaDownloadService {
    override suspend fun download(url: String): Path {
        logger.info("START Download remote file. url: {}", url)
        val httpResponse = resourceResolveService.resolve(url).body()
        val createTempFile = Files.createTempFile("hideout-remote-download", ".tmp")

        logger.debug("Save to {} url: {} ", createTempFile, url)

        httpResponse.use { inputStream ->
            createTempFile.outputStream().use {
                inputStream.transferTo(it)
            }
        }

        logger.info("SUCCESS Download remote file. url: {}", url)
        return createTempFile
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RemoteMediaDownloadServiceImpl::class.java)
    }
}
