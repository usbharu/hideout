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

package dev.usbharu.hideout.core.domain.model.actorinstancerelationship

import dev.usbharu.hideout.core.domain.event.actorinstancerelationship.ActorInstanceRelationshipDomainEventFactory
import dev.usbharu.hideout.core.domain.event.actorinstancerelationship.ActorInstanceRelationshipEvent.*
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable

class ActorInstanceRelationship(
    val actorId: ActorId,
    val instanceId: InstanceId,
    blocking: Boolean = false,
    muting: Boolean = false,
    doNotSendPrivate: Boolean = false,
) : DomainEventStorable() {
    var doNotSendPrivate = doNotSendPrivate
        private set
    var muting = muting
        private set
    var blocking = blocking
        private set

    fun block(): ActorInstanceRelationship {
        addDomainEvent(ActorInstanceRelationshipDomainEventFactory(this).createEvent(BLOCK))
        blocking = true
        return this
    }

    fun unblock(): ActorInstanceRelationship {
        blocking = false
        return this
    }

    fun mute(): ActorInstanceRelationship {
        addDomainEvent(ActorInstanceRelationshipDomainEventFactory(this).createEvent(MUTE))
        muting = true
        return this
    }

    fun unmute(): ActorInstanceRelationship {
        addDomainEvent(ActorInstanceRelationshipDomainEventFactory(this).createEvent(UNMUTE))
        muting = false
        return this
    }

    fun doNotSendPrivate(): ActorInstanceRelationship {
        doNotSendPrivate = true
        return this
    }

    fun doSendPrivate(): ActorInstanceRelationship {
        doNotSendPrivate = false
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActorInstanceRelationship

        if (actorId != other.actorId) return false
        if (instanceId != other.instanceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = actorId.hashCode()
        result = 31 * result + instanceId.hashCode()
        return result
    }

    override fun toString(): String {
        return "ActorInstanceRelationship(" +
                "actorId=$actorId, " +
                "instanceId=$instanceId, " +
                "blocking=$blocking, " +
                "muting=$muting, " +
                "doNotSendPrivate=$doNotSendPrivate" +
                ")"
    }

    companion object {
        fun default(actorId: ActorId, instanceId: InstanceId): ActorInstanceRelationship {
            return ActorInstanceRelationship(
                actorId = actorId,
                instanceId = instanceId,
                blocking = false,
                muting = false,
                doNotSendPrivate = false
            )
        }
    }
}
