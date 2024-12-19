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

import dev.usbharu.hideout.core.application.domainevent.subscribers.RegisterHomeTimeline
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.timeline.*
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserRegisterHomeTimelineApplicationService(
    private val userDetailRepository: UserDetailRepository,
    private val timelineRepository: TimelineRepository,
    private val idGenerateService: IdGenerateService,
    transaction: Transaction,
    private val timelineRelationshipRepository: TimelineRelationshipRepository
) : AbstractApplicationService<RegisterHomeTimeline, Unit>(transaction, logger) {
    override suspend fun internalExecute(command: RegisterHomeTimeline, principal: Principal) {
        val userDetail = (
            userDetailRepository.findById(UserDetailId(command.userDetailId))
                ?: throw IllegalArgumentException("UserDetail ${command.userDetailId} not found.")
            )

        val timeline = Timeline.create(
            TimelineId(idGenerateService.generateId()),
            UserDetailId(command.userDetailId),
            TimelineName("System-LocalUser-HomeTimeline-${command.userDetailId}"),
            TimelineVisibility.PRIVATE,
            true
        )
        timelineRepository.save(timeline)
        userDetail.homeTimelineId = timeline.id

        val timelineRelationship = TimelineRelationship(
            TimelineRelationshipId(idGenerateService.generateId()),
            timeline.id,
            userDetail.actorId,
            Visible.DIRECT
        )

        timelineRelationshipRepository.save(timelineRelationship)

        userDetailRepository.save(userDetail)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserRegisterHomeTimelineApplicationService::class.java)
    }
}
