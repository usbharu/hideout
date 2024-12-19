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

package dev.usbharu.hideout.core.domain.model.reaction

import dev.usbharu.hideout.core.domain.event.reaction.ReactionEvent
import dev.usbharu.hideout.core.domain.event.reaction.ReactionEventFactory
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.time.Instant

class Reaction(
    val id: ReactionId,
    val postId: PostId,
    val actorId: ActorId,
    val customEmojiId: CustomEmojiId?,
    val unicodeEmoji: UnicodeEmoji,
    val createdAt: Instant
) : DomainEventStorable() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reaction

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    fun delete() {
        addDomainEvent(ReactionEventFactory(this).createEvent(ReactionEvent.DELETE))
    }

    companion object {
        @Suppress("LongParameterList")
        fun create(
            id: ReactionId,
            postId: PostId,
            actorId: ActorId,
            customEmojiId: CustomEmojiId?,
            unicodeEmoji: UnicodeEmoji,
            createdAt: Instant
        ): Reaction {
            return Reaction(
                id,
                postId,
                actorId,
                customEmojiId,
                unicodeEmoji,
                createdAt
            ).apply { addDomainEvent(ReactionEventFactory(this).createEvent(ReactionEvent.CREATE)) }
        }
    }
}
