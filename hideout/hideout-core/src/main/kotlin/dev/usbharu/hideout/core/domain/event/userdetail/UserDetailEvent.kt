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

package dev.usbharu.hideout.core.domain.event.userdetail

import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class UserDetailDomainEventFactory(private val userDetail: UserDetail) {
    fun createEvent(userDetailEvent: UserDetailEvent): DomainEvent<UserDetailEventBody> {
        return DomainEvent.create(
            userDetailEvent.eventName,
            UserDetailEventBody(userDetail.id)
        )
    }
}

class UserDetailEventBody(userDetail: UserDetailId) : DomainEventBody(
    mapOf(
        "userDetail" to userDetail
    )
) {
    fun getUserDetail(): UserDetailId = toMap()["userDetail"] as UserDetailId
}

enum class UserDetailEvent(val eventName: String) {
    CREATE("UserDetailCreate"),
}
