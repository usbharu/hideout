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

package dev.usbharu.hideout.core.external.timeline

import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.support.timelineobjectdetail.TimelineObjectDetail
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship

interface TimelineStore {
    suspend fun addPost(post: Post)
    suspend fun updatePost(post: Post)
    suspend fun removePost(post: Post)
    suspend fun addTimelineRelationship(timelineRelationship: TimelineRelationship)
    suspend fun removeTimelineRelationship(timelineRelationship: TimelineRelationship)

    suspend fun updateTimelineRelationship(timelineRelationship: TimelineRelationship)
    suspend fun addTimeline(timeline: Timeline, timelineRelationshipList: List<TimelineRelationship>)
    suspend fun removeTimeline(timeline: Timeline)

    suspend fun readTimeline(
        timeline: Timeline,
        option: ReadTimelineOption? = null,
        page: Page? = null,
        principal: Principal
    ): PaginationList<TimelineObjectDetail, PostId>
}
