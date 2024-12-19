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
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserRemoveTimelineRelationshipApplicationService(
    transaction: Transaction,
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
    private val timelineRepository: TimelineRepository
) :
    LocalUserAbstractApplicationService<RemoveTimelineRelationship, Unit>(
        transaction,
        logger
    ) {

    override suspend fun internalExecute(command: RemoveTimelineRelationship, principal: LocalUser) {
        val timelineRelationship = (
            timelineRelationshipRepository.findById(command.timelineRelationshipId)
                ?: throw IllegalArgumentException("TimelineRelationship ${command.timelineRelationshipId} not found.")
            )

        val timeline = (
            timelineRepository.findById(timelineRelationship.timelineId)
                ?: throw IllegalArgumentException("Timeline ${timelineRelationship.timelineId} not found.")
            )

        if (timeline.userDetailId != principal.userDetailId) {
            throw PermissionDeniedException()
        }

        timelineRelationshipRepository.delete(timelineRelationship)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserRemoveTimelineRelationshipApplicationService::class.java)
    }
}

data class RemoveTimelineRelationship(val timelineRelationshipId: TimelineRelationshipId)
