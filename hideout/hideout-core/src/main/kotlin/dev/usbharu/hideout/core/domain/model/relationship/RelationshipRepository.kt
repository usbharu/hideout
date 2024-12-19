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

package dev.usbharu.hideout.core.domain.model.relationship

import dev.usbharu.hideout.core.domain.model.actor.ActorId

@Suppress("FunctionMaxLength")
interface RelationshipRepository {
    suspend fun save(relationship: Relationship): Relationship
    suspend fun delete(relationship: Relationship)
    suspend fun findByActorIdAndTargetId(actorId: ActorId, targetId: ActorId): Relationship?
    suspend fun findByActorIdsAndTargetIdAndBlocking(
        actorIds: List<ActorId>,
        targetId: ActorId,
        blocking: Boolean
    ): List<Relationship>

    suspend fun findByActorIdAndTargetIdsAndFollowing(
        actorId: ActorId,
        targetIds: List<ActorId>,
        following: Boolean
    ): List<Relationship>
}
