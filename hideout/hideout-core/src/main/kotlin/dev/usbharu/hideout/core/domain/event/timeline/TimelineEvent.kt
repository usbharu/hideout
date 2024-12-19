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

package dev.usbharu.hideout.core.domain.event.timeline

import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class TimelineEventFactory(private val timeline: Timeline) {
    fun createEvent(timelineEvent: TimelineEvent): DomainEvent<TimelineEventBody> =
        DomainEvent.create(timelineEvent.eventName, TimelineEventBody(timeline.id))
}

class TimelineEventBody(timelineId: TimelineId) : DomainEventBody(mapOf("timeline" to timelineId)) {
    fun getTimelineId(): TimelineId = toMap()["timeline"] as TimelineId
}

enum class TimelineEvent(val eventName: String) {
    CHANGE_VISIBILITY("ChangeVisibility"),
    CREATE("TimelineCreate")
}
