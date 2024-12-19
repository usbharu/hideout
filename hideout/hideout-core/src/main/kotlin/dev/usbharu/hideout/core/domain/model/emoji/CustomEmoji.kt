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

package dev.usbharu.hideout.core.domain.model.emoji

import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.support.domain.Domain
import java.net.URI
import java.time.Instant

sealed class Emoji {
    abstract val domain: Domain
    abstract val name: String

    @Suppress("FunctionMinLength")
    abstract fun id(): String
    override fun toString(): String {
        return "Emoji(" +
                "domain='$domain', " +
                "name='$name'" +
                ")"
    }
}

data class CustomEmoji(
    val id: CustomEmojiId,
    override val name: String,
    override val domain: Domain,
    val instanceId: InstanceId,
    val url: URI,
    val category: String?,
    val createdAt: Instant,
) : Emoji() {
    override fun id(): String = id.toString()
}

data class UnicodeEmoji(
    override val name: String
) : Emoji() {
    override val domain: Domain = Companion.domain
    override fun id(): String = name

    companion object {
        val domain = Domain("unicode.org")
    }
}
