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

package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.timeline.*
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserRegisterTimelineApplicationService(
    private val idGenerateService: IdGenerateService,
    private val timelineRepository: TimelineRepository,
    transaction: Transaction
) :
    LocalUserAbstractApplicationService<RegisterTimeline, TimelineId>(transaction, logger) {
    override suspend fun internalExecute(command: RegisterTimeline, principal: LocalUser): TimelineId {
        val timeline = Timeline.create(
            id = TimelineId(idGenerateService.generateId()),
            userDetailId = principal.userDetailId,
            name = TimelineName(command.timelineName),
            visibility = command.visibility,
            isSystem = false
        )

        timelineRepository.save(timeline)
        return timeline.id
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserRegisterTimelineApplicationService::class.java)
    }
}

data class RegisterTimeline(
    val timelineName: String,
    val visibility: TimelineVisibility
)
