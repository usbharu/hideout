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

package dev.usbharu.hideout.core.domain.model.timeline

import dev.usbharu.hideout.core.domain.event.timeline.TimelineEvent
import dev.usbharu.hideout.core.domain.event.timeline.TimelineEventFactory
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable

class Timeline(
    val id: TimelineId,
    val userDetailId: UserDetailId,
    name: TimelineName,
    visibility: TimelineVisibility,
    val isSystem: Boolean
) : DomainEventStorable() {
    var visibility = visibility
        private set

    var name = name
        private set

    fun setVisibility(visibility: TimelineVisibility, userDetail: UserDetail) {
        check(isSystem.not())
        require(userDetailId == userDetail.id)
        this.visibility = visibility
        addDomainEvent(TimelineEventFactory(this).createEvent(TimelineEvent.CHANGE_VISIBILITY))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Timeline

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    companion object {
        fun create(
            id: TimelineId,
            userDetailId: UserDetailId,
            name: TimelineName,
            visibility: TimelineVisibility,
            isSystem: Boolean
        ): Timeline {
            val timeline = Timeline(
                id = id,
                userDetailId = userDetailId,
                name = name,
                visibility = visibility,
                isSystem = isSystem
            )
            timeline.addDomainEvent(TimelineEventFactory(timeline).createEvent(TimelineEvent.CREATE))
            return timeline
        }
    }
}
