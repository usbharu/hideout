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

package dev.usbharu.hideout.core.domain.model.userdetails

import dev.usbharu.hideout.core.domain.event.userdetail.UserDetailDomainEventFactory
import dev.usbharu.hideout.core.domain.event.userdetail.UserDetailEvent
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.time.Instant

class UserDetail(
    val id: UserDetailId,
    val actorId: ActorId,
    var password: UserDetailHashedPassword,
    var autoAcceptFolloweeFollowRequest: Boolean,
    var lastMigration: Instant? = null,
    var homeTimelineId: TimelineId?
) : DomainEventStorable() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserDetail

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String {
        return "UserDetail(" +
            "id=$id, " +
            "actorId=$actorId, " +
            "password=$password, " +
            "autoAcceptFolloweeFollowRequest=$autoAcceptFolloweeFollowRequest, " +
            "lastMigration=$lastMigration, " +
            "homeTimelineId=$homeTimelineId" +
            ")"
    }

    companion object {
        @Suppress("LongParameterList")
        fun create(
            id: UserDetailId,
            actorId: ActorId,
            password: UserDetailHashedPassword,
            autoAcceptFolloweeFollowRequest: Boolean = false,
            lastMigration: Instant? = null,
            homeTimelineId: TimelineId? = null
        ): UserDetail {
            val userDetail = UserDetail(
                id,
                actorId,
                password,
                autoAcceptFolloweeFollowRequest,
                lastMigration,
                homeTimelineId
            )
            userDetail.addDomainEvent(UserDetailDomainEventFactory(userDetail).createEvent(UserDetailEvent.CREATE))
            return userDetail
        }
    }
}
