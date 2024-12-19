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

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
open class JsonLd {
    @JsonProperty("@context")
    @JsonDeserialize(contentUsing = ContextDeserializer::class)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY, using = ContextSerializer::class)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var context: List<String> = emptyList()
        set(value) {
            field = value.filterNotNull().filter { it.isNotBlank() }
        }

    @JsonCreator
    constructor(context: List<String?>?) {
        if (context != null) {
            this.context = context.filterNotNull().filter { it.isNotBlank() }
        } else {
            this.context = emptyList()
        }
    }

    protected constructor()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonLd) return false

        return context == other.context
    }

    override fun hashCode(): Int = context.hashCode()

    override fun toString(): String = "JsonLd(context=$context)"
}

class ContextDeserializer : JsonDeserializer<String>() {

    override fun deserialize(
        p0: com.fasterxml.jackson.core.JsonParser?,
        p1: com.fasterxml.jackson.databind.DeserializationContext?
    ): String {
        val readTree: JsonNode = p0?.codec?.readTree(p0) ?: return ""
        if (readTree.isValueNode) {
            return readTree.textValue()
        }
        return ""
    }
}

class ContextSerializer : JsonSerializer<List<String>>() {

    @Deprecated("Deprecated in Java")
    override fun isEmpty(value: List<String>?): Boolean = value.isNullOrEmpty()

    override fun isEmpty(provider: SerializerProvider?, value: List<String>?): Boolean = value.isNullOrEmpty()

    override fun serialize(value: List<String>?, gen: JsonGenerator?, serializers: SerializerProvider) {
        if (value.isNullOrEmpty()) {
            serializers.defaultSerializeNull(gen)
            return
        }
        if (value.size == 1) {
            gen?.writeString(value[0])
        } else {
            gen?.writeStartArray()
            value.forEach {
                gen?.writeString(it)
            }
            gen?.writeEndArray()
        }
    }
}
