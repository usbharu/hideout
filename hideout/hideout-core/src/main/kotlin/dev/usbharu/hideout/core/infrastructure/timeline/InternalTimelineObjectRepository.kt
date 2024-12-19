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

package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject

interface InternalTimelineObjectRepository {
    suspend fun save(timelineObject: TimelineObject): TimelineObject

    suspend fun saveAll(timelineObjectList: List<TimelineObject>): List<TimelineObject>

    suspend fun findByPostId(postId: PostId): List<TimelineObject>

    suspend fun deleteByPostId(postId: PostId)

    suspend fun deleteByTimelineIdAndActorId(timelineId: TimelineId, actorId: ActorId)

    suspend fun deleteByTimelineId(timelineId: TimelineId)

    /**
     * 指定したTimelineIdより大きく、近いものを返す
     */
    suspend fun findByTimelineIdAndPostIdGT(timelineId: TimelineId, postId: PostId): TimelineObject?

    /**
     * 指定したTimelineIdより小さく、近いものを返す
     */
    suspend fun findByTimelineIdAndPostIdLT(timelineId: TimelineId, postId: PostId): TimelineObject?

    suspend fun findByTimelineId(
        timelineId: TimelineId,
        internalTimelineObjectOption: InternalTimelineObjectOption? = null,
        page: Page? = null
    ): PaginationList<TimelineObject, PostId>
}

data class InternalTimelineObjectOption(
    val localOnly: Boolean? = null,
    val remoteOnly: Boolean? = null,
    val mediaOnly: Boolean? = null
)
