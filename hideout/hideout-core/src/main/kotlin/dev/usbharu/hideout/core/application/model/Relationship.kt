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

package dev.usbharu.hideout.core.application.model

import dev.usbharu.hideout.core.domain.model.actorinstancerelationship.ActorInstanceRelationship
import dev.usbharu.hideout.core.domain.model.relationship.Relationship

data class Relationship(
    val actorId: Long,
    val targetId: Long,
    val following: Boolean,
    val followedBy: Boolean,
    val blocking: Boolean,
    val blockedBy: Boolean,
    val muting: Boolean,
    val followRequesting: Boolean,
    val followRequestedBy: Boolean,
    val domainBlocking: Boolean,
    val domainMuting: Boolean,
    val domainDoNotSendPrivate: Boolean,
) {
    companion object {
        fun of(
            relationship: Relationship,
            relationship2: Relationship,
            actorInstanceRelationship: ActorInstanceRelationship,
        ): dev.usbharu.hideout.core.application.model.Relationship {
            return Relationship(
                actorId = relationship.actorId.id,
                targetId = relationship.targetActorId.id,
                following = relationship.following,
                followedBy = relationship2.following,
                blocking = relationship.blocking,
                blockedBy = relationship2.blocking,
                muting = relationship.muting,
                followRequesting = relationship.followRequesting,
                followRequestedBy = relationship2.followRequesting,
                domainBlocking = actorInstanceRelationship.blocking,
                domainMuting = actorInstanceRelationship.muting,
                domainDoNotSendPrivate = actorInstanceRelationship.doNotSendPrivate
            )
        }
    }
}
