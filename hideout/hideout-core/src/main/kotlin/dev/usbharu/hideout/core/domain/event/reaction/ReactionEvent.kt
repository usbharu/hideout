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

package dev.usbharu.hideout.core.domain.event.reaction

import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class ReactionEventFactory(private val reaction: Reaction) {
    fun createEvent(reactionEvent: ReactionEvent): DomainEvent<ReactionEventBody> =
        DomainEvent.create(reactionEvent.eventName, ReactionEventBody(reaction))
}

class ReactionEventBody(
    reaction: Reaction
) : DomainEventBody(mapOf("reactionId" to reaction.id)) {
    fun getReactionId(): ReactionId = toMap()["reactionId"] as ReactionId
}

enum class ReactionEvent(val eventName: String) {
    CREATE("ReactionCreate"),
    DELETE("ReactionDelete"),
}
