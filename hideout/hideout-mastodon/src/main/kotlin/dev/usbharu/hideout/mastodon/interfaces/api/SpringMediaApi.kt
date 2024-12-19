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

package dev.usbharu.hideout.mastodon.interfaces.api

import dev.usbharu.hideout.core.application.media.UploadMedia
import dev.usbharu.hideout.core.application.media.UploadMediaApplicationService
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.SpringSecurityOauth2PrincipalContextHolder
import dev.usbharu.hideout.mastodon.interfaces.api.generated.MediaApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.MediaAttachment
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files

@Controller
class SpringMediaApi(
    private val uploadMediaApplicationService: UploadMediaApplicationService,
    private val principalContextHolder: SpringSecurityOauth2PrincipalContextHolder
) : MediaApi {
    override suspend fun apiV1MediaPost(
        file: MultipartFile,
        thumbnail: MultipartFile?,
        description: String?,
        focus: String?,
    ): ResponseEntity<MediaAttachment> {
        if (file.size == 0L) {
            logger.warn("File is empty.")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty.")
        }

        val tempFile = Files.createTempFile("hideout-tmp-file", ".tmp")

        Files.newOutputStream(tempFile).use { outputStream ->
            file.inputStream.use {
                it.transferTo(outputStream)
            }
        }

        val media = uploadMediaApplicationService.execute(
            UploadMedia(
                tempFile,
                file.originalFilename ?: file.name,
                null,
                description
            ),
            principalContextHolder.getPrincipal()
        )

        return ResponseEntity.ok(
            MediaAttachment(
                id = media.mediaId.toString(),
                type = when (media.type) {
                    "Image" -> MediaAttachment.Type.image
                    "Video" -> MediaAttachment.Type.video
                    "Audio" -> MediaAttachment.Type.audio
                    else -> MediaAttachment.Type.unknown
                },
                url = media.url.toString(),
                previewUrl = media.thumbnailUrl?.toString(),
                remoteUrl = null,
                description = media.description,
                blurhash = media.blurhash,
                textUrl = media.url.toASCIIString()
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SpringMediaApi::class.java)
    }
}
