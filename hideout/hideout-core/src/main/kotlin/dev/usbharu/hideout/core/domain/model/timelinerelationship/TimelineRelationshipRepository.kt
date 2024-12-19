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

package dev.usbharu.hideout.core.domain.model.timelinerelationship

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId

interface TimelineRelationshipRepository {
    suspend fun save(timelineRelationship: TimelineRelationship): TimelineRelationship
    suspend fun delete(timelineRelationship: TimelineRelationship)

    suspend fun findByActorId(actorId: ActorId): List<TimelineRelationship>
    suspend fun findById(timelineRelationshipId: TimelineRelationshipId): TimelineRelationship?
    suspend fun findByTimelineIdAndActorId(timelineId: TimelineId, actorId: ActorId): TimelineRelationship?
}
