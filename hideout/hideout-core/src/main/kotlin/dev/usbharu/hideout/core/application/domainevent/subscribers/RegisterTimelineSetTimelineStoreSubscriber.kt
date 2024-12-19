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

package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.timeline.SetTimelineToTimelineStoreApplicationService
import dev.usbharu.hideout.core.application.timeline.SetTimleineStore
import dev.usbharu.hideout.core.domain.event.timeline.TimelineEvent
import dev.usbharu.hideout.core.domain.event.timeline.TimelineEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import org.springframework.stereotype.Component

@Component
class RegisterTimelineSetTimelineStoreSubscriber(
    private val domainEventSubscriber: DomainEventSubscriber,
    private val setTimelineToTimelineStoreApplicationService: SetTimelineToTimelineStoreApplicationService
) : Subscriber, DomainEventConsumer<TimelineEventBody> {

    override fun init() {
        domainEventSubscriber.subscribe<TimelineEventBody>(TimelineEvent.CREATE.eventName, this)
    }

    override suspend fun invoke(p1: DomainEvent<TimelineEventBody>) {
        setTimelineToTimelineStoreApplicationService.execute(SetTimleineStore(p1.body.getTimelineId()), Anonymous)
    }
}
