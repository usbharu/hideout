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

package dev.usbharu.hideout.core.domain.event.relationship

import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class RelationshipEventFactory(private val relationship: Relationship, private val principal: Principal = Anonymous) {
    fun createEvent(relationshipEvent: RelationshipEvent): DomainEvent<RelationshipEventBody> =
        DomainEvent.create(relationshipEvent.eventName, RelationshipEventBody(relationship, principal))
}

class RelationshipEventBody(
    relationship: Relationship,
    override val principal: Principal
) : DomainEventBody(mapOf("relationship" to relationship), principal) {
    fun getRelationship(): Relationship = toMap()["relationship"] as Relationship
}

enum class RelationshipEvent(val eventName: String) {
    ACCEPT_FOLLOW("RelationshipFollow"),
    REJECT_FOLLOW("RelationshipRejectFollow"),
    UNFOLLOW("RelationshipUnfollow"),
    BLOCK("RelationshipBlock"),
    UNBLOCK("RelationshipUnblock"),
    MUTE("RelationshipMute"),
    UNMUTE("RelationshipUnmute"),
    FOLLOW_REQUEST("RelationshipFollowRequest"),
    UNFOLLOW_REQUEST("RelationshipUnfollowRequest"),
}
