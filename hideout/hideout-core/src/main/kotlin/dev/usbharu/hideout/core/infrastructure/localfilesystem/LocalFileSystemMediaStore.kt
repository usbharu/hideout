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

package dev.usbharu.hideout.core.infrastructure.localfilesystem

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.config.LocalStorageConfig
import dev.usbharu.hideout.core.external.mediastore.MediaStore
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.copyTo

@Component
@ConditionalOnProperty("hideout.storage.type", havingValue = "local", matchIfMissing = true)
class LocalFileSystemMediaStore(
    localStorageConfig: LocalStorageConfig,
    applicationConfig: ApplicationConfig
) :
    MediaStore {

    private val publicUrl = localStorageConfig.publicUrl ?: "${applicationConfig.url}/files/"

    private val savePath = Path.of(localStorageConfig.path)

    init {
        Files.createDirectories(savePath)
    }

    override suspend fun upload(path: Path, id: String): URI {
        logger.info("START Media upload. {}", id)
        val fileSavePath = buildSavePath(savePath, id)

        val fileSavePathString = fileSavePath.toAbsolutePath().toString()
        logger.info("MEDIA save. path: {}", fileSavePathString)

        @Suppress("TooGenericExceptionCaught")
        try {
            path.copyTo(fileSavePath)
        } catch (e: Exception) {
            logger.warn("FAILED to Save the media.", e)
            throw e
        }

        logger.info("SUCCESS Media upload. {}", id)
        return URI.create(publicUrl).resolve(id)
    }

    private fun buildSavePath(savePath: Path, name: String): Path = savePath.resolve(name)

    companion object {
        private val logger = LoggerFactory.getLogger(LocalFileSystemMediaStore::class.java)
    }
}
