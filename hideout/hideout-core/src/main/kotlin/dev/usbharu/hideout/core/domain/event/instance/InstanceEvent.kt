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

package dev.usbharu.hideout.core.domain.event.instance

import dev.usbharu.hideout.core.domain.model.instance.Instance
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class InstanceEventFactory(private val instance: Instance) {
    fun createEvent(event: InstanceEvent): DomainEvent<InstanceEventBody> {
        return DomainEvent.create(
            event.eventName,
            InstanceEventBody(instance)
        )
    }
}

class InstanceEventBody(instance: Instance) : DomainEventBody(mapOf("instance" to instance))

enum class InstanceEvent(val eventName: String) {
    UPDATE("InstanceUpdate")
}
