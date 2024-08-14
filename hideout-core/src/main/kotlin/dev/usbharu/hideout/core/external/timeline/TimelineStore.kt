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
