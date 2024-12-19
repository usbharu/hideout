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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

open class Announce @JsonCreator constructor(
    type: List<String> = emptyList(),
    @JsonProperty("object")
    val apObject: String,
    override val actor: String,
    override val id: String,
    val published: String,
    val to: List<String> = emptyList(),
    val cc: List<String> = emptyList()
) : Object(
    type = add(type, "Announce")
),
    HasActor,
    HasId {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Announce

        if (apObject != other.apObject) return false
        if (actor != other.actor) return false
        if (id != other.id) return false
        if (published != other.published) return false
        if (to != other.to) return false
        if (cc != other.cc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + apObject.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + published.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + cc.hashCode()
        return result
    }

    override fun toString(): String {
        return "Announce(" +
                "apObject='$apObject', " +
                "actor='$actor', " +
                "id='$id', " +
                "published='$published', " +
                "to=$to, " +
                "cc=$cc" +
                ")" +
                " ${super.toString()}"
    }
}
