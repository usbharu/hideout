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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize

open class Object : JsonLd {
    @JsonSerialize(using = TypeSerializer::class)
    var type: List<String> = emptyList()
        set(value) {
            field = value.filter { it.isNotBlank() }
        }

    protected constructor()
    constructor(type: List<String>) : super() {
        this.type = type.filter { it.isNotBlank() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Object

        return type == other.type
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String = "Object(type=$type) ${super.toString()}"

    companion object {
        @JvmStatic
        protected fun add(list: List<String>, type: String): List<String> {
            val toMutableList = list.toMutableList()
            toMutableList.add(type)
            return toMutableList.distinct()
        }
    }
}

class TypeSerializer : JsonSerializer<List<String>>() {
    override fun serialize(value: List<String>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (value?.size == 1) {
            gen?.writeString(value[0])
        } else {
            gen?.writeStartArray()
            value?.forEach {
                gen?.writeString(it)
            }
            gen?.writeEndArray()
        }
    }
}
