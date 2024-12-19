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

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserAddTimelineRelationshipApplicationService(
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
    private val timelineRepository: TimelineRepository,
    private val idGenerateService: IdGenerateService,
    transaction: Transaction
) :
    LocalUserAbstractApplicationService<AddTimelineRelationship, Unit>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: AddTimelineRelationship, principal: LocalUser) {
        val timeline = timelineRepository.findById(command.timelineId)
            ?: throw IllegalArgumentException("Timeline ${command.timelineId} not found.")

        if (timeline.userDetailId != principal.userDetailId) {
            throw PermissionDeniedException()
        }

        timelineRelationshipRepository.save(
            TimelineRelationship(
                TimelineRelationshipId(idGenerateService.generateId()),
                command.timelineId,
                command.actorId,
                command.visible
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserAddTimelineRelationshipApplicationService::class.java)
    }
}

data class AddTimelineRelationship(
    val timelineId: TimelineId,
    val actorId: ActorId,
    val visible: Visible
)
