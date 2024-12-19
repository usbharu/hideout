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

package dev.usbharu.hideout.core.domain.event.actor

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class ActorDomainEventFactory(private val actor: Actor) {
    fun createEvent(actorEvent: ActorEvent): DomainEvent<ActorEventBody> {
        return DomainEvent.create(
            actorEvent.eventName,
            ActorEventBody(actor.id),
            actorEvent.collectable
        )
    }
}

class ActorEventBody(actor: ActorId) : DomainEventBody(
    mapOf(
        "actor" to actor
    ),
)

enum class ActorEvent(val eventName: String, val collectable: Boolean = true) {
    UPDATE("ActorUpdate"),
    DELETE("ActorDelete"),
    CHECK_UPDATE("ActorCheckUpdate"),
    MOVE("ActorMove"),
    ACTOR_SUSPEND("ActorSuspend"),
    ACTOR_UNSUSPEND("ActorUnsuspend"),
}
