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

package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.timeline.RemoveTimelineRelationship
import dev.usbharu.hideout.core.application.timeline.UserRemoveTimelineRelationshipApplicationService
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEvent
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEventBody
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TimelineRelationshipUnfollowSubscriber(
    private val domainEventSubscriber: DomainEventSubscriber,
    private val userRemoveTimelineRelationshipApplicationService: UserRemoveTimelineRelationshipApplicationService,
    private val userDetailRepository: UserDetailRepository,
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
) : Subscriber, DomainEventConsumer<RelationshipEventBody> {
    override fun init() {
        domainEventSubscriber.subscribe<RelationshipEventBody>(RelationshipEvent.UNFOLLOW.eventName, this)
    }

    override suspend fun invoke(p1: DomainEvent<RelationshipEventBody>) {
        val relationship = p1.body.getRelationship()
        val userDetail = userDetailRepository.findByActorId(relationship.actorId.id) ?: throw IllegalStateException(
            "UserDetail ${relationship.actorId} not found by actorId."
        )
        if (userDetail.homeTimelineId == null) {
            logger.warn("HomeTimeline for ${userDetail.id} not found.")
            return
        }

        val timelineRelationship = timelineRelationshipRepository.findByTimelineIdAndActorId(
            userDetail.homeTimelineId!!,
            relationship.targetActorId
        )
            ?: throw IllegalStateException("TimelineRelationship ${userDetail.homeTimelineId} to ${relationship.targetActorId} not found by timelineId and ActorId")

        userRemoveTimelineRelationshipApplicationService.execute(
            RemoveTimelineRelationship(timelineRelationship.id),
            p1.body.principal
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TimelineRelationshipUnfollowSubscriber::class.java)
    }
}
