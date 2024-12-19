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

package dev.usbharu.hideout.core.application.media

import dev.usbharu.hideout.core.application.model.MediaDetail
import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.media.*
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.external.media.MediaProcessor
import dev.usbharu.hideout.core.external.mediastore.MediaStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.net.URI
import java.nio.file.Path
import dev.usbharu.hideout.core.domain.model.media.Media as MediaModel

@Service
class UploadMediaApplicationService(
    @Qualifier("delegate") private val mediaProcessor: MediaProcessor,
    private val mediaStore: MediaStore,
    private val mediaRepository: MediaRepository,
    private val idGenerateService: IdGenerateService,
    transaction: Transaction
) : LocalUserAbstractApplicationService<UploadMedia, MediaDetail>(
    transaction,
    logger
) {
    override suspend fun internalExecute(command: UploadMedia, principal: LocalUser): MediaDetail {
        val process = mediaProcessor.process(command.path, command.name, null)
        val id = idGenerateService.generateId()
        val thumbnailUri = if (process.thumbnailPath != null) {
            mediaStore.upload(process.thumbnailPath, "thumbnail-$id.${process.mimeType.subtype}")
        } else {
            null
        }
        val uri = mediaStore.upload(process.path, "$id.${process.mimeType.subtype}")

        val media = MediaModel(
            id = MediaId(id),
            name = MediaName(command.name),
            url = uri,
            remoteUrl = command.remoteUri,
            thumbnailUrl = thumbnailUri,
            type = process.fileType,
            mimeType = process.mimeType,
            blurHash = process.blurHash?.let { MediaBlurHash(it) },
            description = command.description?.let { MediaDescription(it) },
            actorId = principal.actorId
        )

        mediaRepository.save(media)

        return MediaDetail.of(media)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UploadMediaApplicationService::class.java)
    }
}

data class UploadMedia(
    val path: Path,
    val name: String,
    val remoteUri: URI?,
    val description: String?
)
