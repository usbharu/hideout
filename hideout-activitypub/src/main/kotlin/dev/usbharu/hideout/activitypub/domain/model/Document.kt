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

package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

open class Document(
    type: List<String> = emptyList(),
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    override val name: String = "",
    val mediaType: String,
    val url: String
) : Object(
    type = add(type, "Document")
),
    HasName {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Document

        if (mediaType != other.mediaType) return false
        if (url != other.url) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String = "Document(mediaType=$mediaType, url=$url, name='$name') ${super.toString()}"
}
