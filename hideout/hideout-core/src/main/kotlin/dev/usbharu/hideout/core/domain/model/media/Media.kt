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

package dev.usbharu.hideout.core.domain.model.media

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import java.net.URI

@Suppress("LongParameterList")
class Media(
    val id: MediaId,
    val name: MediaName,
    url: URI,
    val remoteUrl: URI?,
    val thumbnailUrl: URI?,
    val type: FileType,
    val mimeType: MimeType,
    val blurHash: MediaBlurHash?,
    val description: MediaDescription? = null,
    val actorId: ActorId,
) {
    var url = url
        private set

    fun setUrl(url: URI) {
        this.url = url
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Media

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Media(" +
            "id=$id, " +
            "name=$name, " +
            "remoteUrl=$remoteUrl, " +
            "thumbnailUrl=$thumbnailUrl, " +
            "type=$type, " +
            "mimeType=$mimeType, " +
            "blurHash=$blurHash, " +
            "description=$description, " +
            "actorId=$actorId, " +
            "url=$url" +
            ")"
    }
}
