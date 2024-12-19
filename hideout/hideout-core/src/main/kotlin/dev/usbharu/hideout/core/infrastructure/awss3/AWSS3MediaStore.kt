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

package dev.usbharu.hideout.core.infrastructure.awss3

import dev.usbharu.hideout.core.config.S3StorageConfig
import dev.usbharu.hideout.core.external.mediastore.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.nio.file.Path

@Component
@ConditionalOnProperty("hideout.storage.type", havingValue = "s3")
class AWSS3MediaStore(
    private val s3StorageConfig: S3StorageConfig,
    private val s3Client: S3Client
) : MediaStore {
    override suspend fun upload(path: Path, id: String): URI {
        logger.info("MEDIA upload. {}", id)

        val fileUploadRequest = PutObjectRequest.builder()
            .bucket(s3StorageConfig.bucket)
            .key(id)
            .build()

        logger.info("MEDIA upload. bucket: {} key: {}", s3StorageConfig.bucket, id)

        withContext(Dispatchers.IO) {
            s3Client.putObject(fileUploadRequest, RequestBody.fromFile(path))
        }
        val successSavedMedia = URI.create("${s3StorageConfig.publicUrl}/${s3StorageConfig.bucket}/$id")

        logger.info("SUCCESS Media upload. {}", id)
        logger.debug(
            "name: {} url: {}",
            id,
            successSavedMedia,
        )

        return successSavedMedia
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AWSS3MediaStore::class.java)
    }
}
