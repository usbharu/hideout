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

package dev.usbharu.hideout.core.infrastructure.media.video

import dev.usbharu.hideout.core.config.FFmpegVideoConfig
import dev.usbharu.hideout.core.domain.model.media.FileType
import dev.usbharu.hideout.core.domain.model.media.MimeType
import dev.usbharu.hideout.core.external.media.MediaProcessor
import dev.usbharu.hideout.core.external.media.ProcessedMedia
import dev.usbharu.hideout.core.infrastructure.media.common.GenerateBlurhash
import org.bytedeco.javacv.FFmpegFrameFilter
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.min

@Component
@Qualifier("video")
class FFmpegVideoProcessor(
    private val fFmpegVideoConfig: FFmpegVideoConfig,
    private val generateBlurhash: GenerateBlurhash
) : MediaProcessor {
    override fun isSupported(mimeType: MimeType): Boolean =
        mimeType.fileType == FileType.Video || mimeType.type == "video"

    @Suppress("LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod", "NestedBlockDepth")
    override suspend fun process(path: Path, filename: String, mimeType: MimeType?): ProcessedMedia {
        val tempFile = Files.createTempFile("hideout-movie-processor-", ".tmp")
        val thumbnailFile = Files.createTempFile("hideout-movie-thumbnail-generate-", ".tmp")
        logger.info("START Convert Movie Media {}", filename)
        var bufferedImage: BufferedImage? = null
        FFmpegFrameGrabber(path.toFile()).use { grabber ->
            grabber.start()
            val width = min(fFmpegVideoConfig.maxWidth, grabber.imageWidth)
            val height = min(fFmpegVideoConfig.maxHeight, grabber.imageHeight)
            val frameRate = fFmpegVideoConfig.frameRate

            logger.debug("Movie Media Width {}, Height {}", width, height)

            FFmpegFrameFilter(
                "fps=fps=$frameRate",
                "anull",
                width,
                height,
                grabber.audioChannels
            ).use { filter ->

                filter.sampleFormat = grabber.sampleFormat
                filter.sampleRate = grabber.sampleRate
                filter.pixelFormat = grabber.pixelFormat
                filter.frameRate = grabber.frameRate
                filter.start()

                val videoBitRate = min(fFmpegVideoConfig.maxBitrate, (width * height * frameRate * 1 * 0.07).toInt())

                logger.debug("Movie Media BitRate {}", videoBitRate)

                FFmpegFrameRecorder(tempFile.toFile(), width, height, grabber.audioChannels).use {
                    it.sampleRate = grabber.sampleRate
                    it.format = fFmpegVideoConfig.format
                    it.videoCodec = fFmpegVideoConfig.videoCodec
                    it.audioCodec = fFmpegVideoConfig.audioCodec
                    it.audioChannels = grabber.audioChannels
                    it.videoQuality = fFmpegVideoConfig.videoQuality
                    it.frameRate = frameRate.toDouble()
                    it.setVideoOption("preset", "ultrafast")
                    it.timestamp = 0
                    it.gopSize = frameRate
                    it.videoBitrate = videoBitRate
                    it.start()

                    val frameConverter = Java2DFrameConverter()

                    while (true) {
                        val grab = grabber.grab() ?: break

                        if (bufferedImage == null) {
                            bufferedImage = frameConverter.convert(grab)
                        }

                        if (grab.image != null || grab.samples != null) {
                            filter.push(grab)
                        }
                        while (true) {
                            val frame = filter.pull() ?: break
                            it.record(frame)
                        }
                    }

                    if (bufferedImage != null) {
                        ImageIO.write(bufferedImage, "jpeg", thumbnailFile.toFile())
                    }
                }
            }
        }

        logger.info("SUCCESS Convert Movie Media {}", filename)

        return ProcessedMedia(
            tempFile,
            thumbnailFile,
            FileType.Video,
            MimeType("video", fFmpegVideoConfig.format, FileType.Video),
            bufferedImage?.let { generateBlurhash.generateBlurhash(it) }
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FFmpegVideoProcessor::class.java)
    }
}
