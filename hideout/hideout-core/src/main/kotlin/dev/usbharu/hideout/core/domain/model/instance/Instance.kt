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

package dev.usbharu.hideout.core.domain.model.instance

import dev.usbharu.hideout.core.domain.event.instance.InstanceEvent
import dev.usbharu.hideout.core.domain.event.instance.InstanceEventFactory
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.net.URI
import java.time.Instant

@Suppress("LongParameterList")
class Instance(
    val id: InstanceId,
    var name: InstanceName,
    var description: InstanceDescription,
    val url: URI,
    iconUrl: URI,
    var sharedInbox: URI?,
    var software: InstanceSoftware,
    var version: InstanceVersion,
    var isBlocked: Boolean,
    var isMuted: Boolean,
    var moderationNote: InstanceModerationNote,
    val createdAt: Instant,
) : DomainEventStorable() {

    var iconUrl = iconUrl
        set(value) {
            addDomainEvent(InstanceEventFactory(this).createEvent(InstanceEvent.UPDATE))
            field = value
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Instance

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
