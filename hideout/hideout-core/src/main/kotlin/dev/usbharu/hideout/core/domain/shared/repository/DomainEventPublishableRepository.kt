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

package dev.usbharu.hideout.core.domain.shared.repository

import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import org.springframework.stereotype.Repository

@Repository
interface DomainEventPublishableRepository<T : DomainEventStorable> {
    val domainEventPublisher: DomainEventPublisher
    suspend fun update(entity: T) {
        entity.getDomainEvents().distinctBy {
            if (it.collectable) {
                it.name
            } else {
                it.id
            }
        }.forEach {
            domainEventPublisher.publishEvent(it)
        }
        entity.clearDomainEvents()
    }
}
