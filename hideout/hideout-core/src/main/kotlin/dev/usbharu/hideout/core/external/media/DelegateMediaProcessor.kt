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

package dev.usbharu.hideout.core.external.media

import dev.usbharu.hideout.core.domain.model.media.MimeType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
@Qualifier("delegate")
class DelegateMediaProcessor(
    private val fileTypeDeterminer: FileTypeDeterminer,
    private val mediaProcessors: List<MediaProcessor>
) : MediaProcessor {
    override fun isSupported(mimeType: MimeType): Boolean = true

    override suspend fun process(path: Path, filename: String, mimeType: MimeType?): ProcessedMedia {
        val fileType = fileTypeDeterminer.fileType(path, filename)
        return mediaProcessors.first { it.isSupported(fileType) }.process(path, filename, fileType)
    }
}
