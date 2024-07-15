package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.external.timeline.TimelineStore

abstract class AbstractTimelineStore(private val idGenerateService: IdGenerateService) : TimelineStore {
    override suspend fun addPost(post: Post) {
        val timelineList = getTimeline(post.actorId)

        val repost = post.repostId?.let { getPost(it) }

        val timelineObjectList = timelineList.map {
            createTimelineObject(post, repost, it)
        }

        insertTimelineObject(timelineObjectList)
    }

    protected abstract suspend fun getTimeline(actorId: ActorId): List<Timeline>

    protected suspend fun createTimelineObject(post: Post, repost: Post?, timeline: Timeline): TimelineObject {
        val filters = getFilters(timeline.userDetailId)

        val applyFilters = applyFilters(post, filters)

        if (repost != null) {
            return TimelineObject.create(
                TimelineObjectId(idGenerateService.generateId()),
                timeline,
                post,
                repost,
                applyFilters.filterResults
            )
        }

        return TimelineObject.create(
            TimelineObjectId(idGenerateService.generateId()),
            timeline,
            post,
            applyFilters.filterResults
        )
    }

    protected abstract suspend fun getFilters(userDetailId: UserDetailId): List<Filter>

    protected abstract suspend fun applyFilters(post: Post, filters: List<Filter>): FilteredPost

    protected abstract suspend fun getPost(postId: PostId): Post?

    protected abstract suspend fun insertTimelineObject(timelineObjectList: List<TimelineObject>)

    protected abstract suspend fun getTimelineObjectByPostId(postId: PostId): List<TimelineObject>

    protected abstract suspend fun removeTimelineObject(postId: PostId)

    override suspend fun updatePost(post: Post) {


        val timelineObjectByPostId = getTimelineObjectByPostId(post.id)

        val repost = post.repostId?.let { getPost(it) }

        if (repost != null) {
            timelineObjectByPostId.map {
                val filters = getFilters(it.userDetailId)
                val applyFilters = applyFilters(post, filters)
                it.updateWith(post, repost, applyFilters.filterResults)
            }
        }
    }

    override suspend fun removePost(post: Post) {
        TODO("Not yet implemented")
    }

    override suspend fun addTimelineRelationship(timelineRelationship: TimelineRelationship) {
        TODO("Not yet implemented")
    }

    override suspend fun removeTimelineRelationship(timelineRelationship: TimelineRelationship) {
        TODO("Not yet implemented")
    }

    override suspend fun addTimeline(timeline: Timeline, timelineRelationshipList: List<TimelineRelationship>) {
        TODO("Not yet implemented")
    }

    override suspend fun removeTimeline(timeline: Timeline) {
        TODO("Not yet implemented")
    }
}