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

import dev.usbharu.hideout.core.application.timeline.AddPost
import dev.usbharu.hideout.core.application.timeline.TimelineAddPostApplicationService
import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.event.post.PostEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import org.springframework.stereotype.Component

@Component
class TimelinePostCreateSubscriber(
    private val timelineAddPostApplicationService: TimelineAddPostApplicationService,
    private val domainEventSubscriber: DomainEventSubscriber,
) : Subscriber, DomainEventConsumer<PostEventBody> {
    override fun init() {
        domainEventSubscriber.subscribe<PostEventBody>(PostEvent.CREATE.eventName, this)
    }

    override suspend fun invoke(p1: DomainEvent<PostEventBody>) {
        timelineAddPostApplicationService.execute(AddPost(p1.body.getPostId()), Anonymous)
    }
}
