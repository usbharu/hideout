/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.infrastructure.media.image

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.config.ImageIOImageConfig
import dev.usbharu.hideout.core.domain.model.media.FileType
import dev.usbharu.hideout.core.domain.model.media.MimeType
import dev.usbharu.hideout.core.external.media.MediaProcessor
import dev.usbharu.hideout.core.external.media.ProcessedMedia
import dev.usbharu.hideout.core.infrastructure.media.common.GenerateBlurhash
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Component
@Qualifier("image")
class ImageIOImageProcessor(
    private val imageIOImageConfig: ImageIOImageConfig,
    private val blurhash: GenerateBlurhash
) : MediaProcessor {
    override fun isSupported(mimeType: MimeType): Boolean =
        mimeType.fileType == FileType.Image || mimeType.type == "image"

    override suspend fun process(path: Path, filename: String, mimeType: MimeType?): ProcessedMedia {
        val read = ImageIO.read(path.inputStream())

        val bufferedImage = BufferedImage(read.width, read.height, BufferedImage.TYPE_INT_RGB)

        val graphics = bufferedImage.createGraphics()

        graphics.drawImage(read, 0, 0, Color.BLACK, null)

        val tempFileName = UUID.randomUUID().toString()
        val tempFile = Files.createTempFile(tempFileName, "tmp")

        val thumbnailPath = run {
            val tempThumbnailFile = Files.createTempFile("thumbnail-$tempFileName", ".tmp")

            tempThumbnailFile.outputStream().use {
                val write = ImageIO.write(
                    Thumbnails.of(bufferedImage)
                        .size(imageIOImageConfig.thumbnailsWidth, imageIOImageConfig.thumbnailsHeight)
                        .imageType(BufferedImage.TYPE_INT_RGB)
                        .asBufferedImage(),
                    imageIOImageConfig.format,
                    it
                )
                tempThumbnailFile.takeIf { write }
            }
        }

        tempFile.outputStream().use {
            if (ImageIO.write(bufferedImage, imageIOImageConfig.format, it).not()) {
                logger.warn("Failed to save a temporary file. type: {} ,path: {}", imageIOImageConfig.format, tempFile)
                throw InternalServerException("Failed to save a temporary file.")
            }
        }

        return ProcessedMedia(
            tempFile,
            thumbnailPath,
            FileType.Image,
            MimeType("image", imageIOImageConfig.format, FileType.Image),
            blurhash.generateBlurhash(bufferedImage)
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ImageIOImageProcessor::class.java)
    }
}
