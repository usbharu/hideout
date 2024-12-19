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

import dev.usbharu.hideout.core.application.model.Timeline
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.domain.model.timeline.TimelineVisibility
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserGetTimelinesApplicationService(transaction: Transaction, private val timelineRepository: TimelineRepository) :
    AbstractApplicationService<GetTimelines, List<Timeline>>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: GetTimelines, principal: Principal): List<Timeline> {
        val userDetailId = UserDetailId(command.userDetailId)

        val timelineVisibility = if (userDetailId == principal.userDetailId) {
            listOf(TimelineVisibility.PUBLIC, TimelineVisibility.UNLISTED, TimelineVisibility.PRIVATE)
        } else {
            listOf(TimelineVisibility.PUBLIC)
        }

        val timelineList =
            timelineRepository.findAllByUserDetailIdAndVisibilityIn(userDetailId, timelineVisibility)

        return timelineList.map { Timeline.of(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserGetTimelinesApplicationService::class.java)
    }
}

data class GetTimelines(val userDetailId: Long)
