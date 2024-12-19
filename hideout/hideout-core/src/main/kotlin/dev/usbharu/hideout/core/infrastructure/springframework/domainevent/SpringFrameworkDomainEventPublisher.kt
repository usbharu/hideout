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

package dev.usbharu.hideout.core.infrastructure.springframework.domainevent

import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.infrastructure.springframework.ApplicationRequestLogInterceptor
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringFrameworkDomainEventPublisher(private val applicationEventPublisher: ApplicationEventPublisher) :
    DomainEventPublisher {
    override suspend fun publishEvent(domainEvent: DomainEvent<*>) {
        logger.trace("Publish ${domainEvent.id} ${domainEvent.name}")

        val requestId: String? = MDC.get(ApplicationRequestLogInterceptor.requestId)
        val springDomainEvent = SpringDomainEvent(
            requestId,
            domainEvent
        )

        applicationEventPublisher.publishEvent(springDomainEvent)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SpringFrameworkDomainEventPublisher::class.java)
    }
}

data class SpringDomainEvent(val requestId: String?, val domainEvent: DomainEvent<*>)
