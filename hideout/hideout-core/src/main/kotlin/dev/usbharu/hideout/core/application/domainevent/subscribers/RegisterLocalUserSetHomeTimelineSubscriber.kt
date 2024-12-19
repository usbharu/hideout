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

import dev.usbharu.hideout.core.application.timeline.UserRegisterHomeTimelineApplicationService
import dev.usbharu.hideout.core.domain.event.userdetail.UserDetailEvent
import dev.usbharu.hideout.core.domain.event.userdetail.UserDetailEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import org.springframework.stereotype.Component

@Component
class RegisterLocalUserSetHomeTimelineSubscriber(
    private val domainEventSubscriber: DomainEventSubscriber,
    private val userRegisterHomeTimelineApplicationService: UserRegisterHomeTimelineApplicationService
) : Subscriber, DomainEventConsumer<UserDetailEventBody> {
    override fun init() {
        domainEventSubscriber.subscribe<UserDetailEventBody>(UserDetailEvent.CREATE.eventName, this)
    }

    override suspend fun invoke(p1: DomainEvent<UserDetailEventBody>) {
        userRegisterHomeTimelineApplicationService.execute(
            RegisterHomeTimeline(p1.body.getUserDetail().id),
            Anonymous
        )
    }
}
