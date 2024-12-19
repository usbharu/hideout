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

package utils

import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable

object AssertDomainEvent {
    fun assertContainsEvent(domainEventStorable: DomainEventStorable, eventName: String) {
        val find = domainEventStorable.getDomainEvents().find { it.name == eventName }

        if (find == null) {
            throw AssertionError("Domain Event not found: $eventName")
        }
    }

    fun assertEmpty(domainEventStorable: DomainEventStorable) {
        if (domainEventStorable.getDomainEvents().isNotEmpty()) {
            throw AssertionError("Domain Event found")
        }
    }
}