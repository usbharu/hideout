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

import dev.usbharu.hideout.core.application.model.Reactions
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.support.timelineobjectdetail.TimelineObjectDetail
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineVisibility
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectWarnFilter
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.external.timeline.ReadTimelineOption
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import java.time.Instant

abstract class AbstractTimelineStore(private val idGenerateService: IdGenerateService) : TimelineStore {
    override suspend fun addPost(post: Post) {
        val timelineList = getTimelines(post.actorId)

        val repost = post.repostId?.let { getPost(it) }
        val replyActorId = post.replyId?.let { getPost(it)?.actorId }

        val timelineObjectList = timelineList.mapNotNull {
            createTimelineObject(post, replyActorId, repost, it)
        }

        insertTimelineObject(timelineObjectList)
    }

    protected abstract suspend fun getTimelines(actorId: ActorId): List<Timeline>

    protected abstract suspend fun getTimeline(timelineId: TimelineId): Timeline?

    protected suspend fun createTimelineObject(
        post: Post,
        replyActorId: ActorId?,
        repost: Post?,
        timeline: Timeline
    ): TimelineObject? {
        if (post.visibility == Visibility.DIRECT) {
            return null
        }
        if (timeline.visibility == TimelineVisibility.PUBLIC && post.visibility != Visibility.PUBLIC) {
            return null
        }
        if (timeline.visibility == TimelineVisibility.UNLISTED &&
            (post.visibility != Visibility.PUBLIC || post.visibility != Visibility.UNLISTED)
        ) {
            return null
        }

        val filters = getFilters(timeline.userDetailId)

        val applyFilters = applyFilters(post, filters)

        if (repost != null) {
            return TimelineObject.create(
                timelineObjectId = TimelineObjectId(idGenerateService.generateId()),
                timeline = timeline,
                post = post,
                replyActorId = replyActorId,
                repost = repost,
                filterResults = applyFilters.filterResults,
                favourited = false
            )
        }

        return TimelineObject.create(
            timelineObjectId = TimelineObjectId(idGenerateService.generateId()),
            timeline = timeline,
            post = post,
            replyActorId = replyActorId,
            filterResults = applyFilters.filterResults,
            favourited = false
        )
    }

    protected abstract suspend fun getFilters(userDetailId: UserDetailId): List<Filter>

    protected abstract suspend fun getNewerFilters(userDetailId: UserDetailId, lastUpdateAt: Instant): List<Filter>

    protected abstract suspend fun applyFilters(post: Post, filters: List<Filter>): FilteredPost

    protected abstract suspend fun getPost(postId: PostId): Post?

    protected abstract suspend fun insertTimelineObject(timelineObjectList: List<TimelineObject>)

    protected abstract suspend fun updateTimelineObject(timelineObjectList: List<TimelineObject>)

    protected abstract suspend fun getTimelineObjectByPostId(postId: PostId): List<TimelineObject>

    protected abstract suspend fun removeTimelineObject(postId: PostId)

    protected abstract suspend fun removeTimelineObject(timelineId: TimelineId, actorId: ActorId)

    protected abstract suspend fun removeTimelineObject(timelineId: TimelineId)

    @Suppress("FunctionMaxLength")
    protected abstract suspend fun getPostsByTimelineRelationshipList(
        timelineRelationshipList: List<TimelineRelationship>
    ): List<Post>

    protected abstract suspend fun getPostsByPostId(postIds: List<PostId>, principal: Principal): List<Post>

    protected abstract suspend fun getTimelineObject(
        timelineId: TimelineId,
        readTimelineOption: ReadTimelineOption?,
        page: Page?
    ): PaginationList<TimelineObject, PostId>

    @Suppress("RedundantHigherOrderMapUsage") // if式に対応していないため
    override suspend fun updatePost(post: Post) {
        val timelineObjectByPostId = getTimelineObjectByPostId(post.id)

        val repost = post.repostId?.let { getPost(it) }

        val timelineObjectList = if (repost != null) {
            timelineObjectByPostId.map {
                val filters = getFilters(it.userDetailId)
                val applyFilters = applyFilters(post, filters)
                it.updateWith(post, repost, applyFilters.filterResults)
                it
            }
        } else {
            timelineObjectByPostId.map {
                val filters = getFilters(it.userDetailId)
                val applyFilters = applyFilters(post, filters)
                it.updateWith(post, applyFilters.filterResults)
                it
            }
        }

        updateTimelineObject(timelineObjectList)
    }

    protected abstract suspend fun getActorPost(actorId: ActorId, visibilityList: List<Visibility>): List<Post>

    override suspend fun removePost(post: Post) {
        removeTimelineObject(post.id)
    }

    override suspend fun addTimelineRelationship(timelineRelationship: TimelineRelationship) {
        val visibilityList = visibilities(timelineRelationship)
        val postList = getActorPost(timelineRelationship.actorId, visibilityList)
        val timeline = getTimeline(timelineRelationship.timelineId) ?: return
        val timelineObjects = postList.mapNotNull { post ->
            val repost = post.repostId?.let { getPost(it) }
            val replyActorId = post.replyId?.let { getPost(it)?.actorId }
            createTimelineObject(post, replyActorId, repost, timeline)
        }

        insertTimelineObject(timelineObjects)
    }

    protected fun visibilities(timelineRelationship: TimelineRelationship): List<Visibility> {
        val visibilityList = when (timelineRelationship.visible) {
            Visible.PUBLIC -> {
                listOf(Visibility.PUBLIC)
            }

            Visible.UNLISTED -> {
                listOf(Visibility.PUBLIC, Visibility.UNLISTED)
            }

            Visible.FOLLOWERS -> {
                listOf(Visibility.PUBLIC, Visibility.UNLISTED, Visibility.FOLLOWERS)
            }

            Visible.DIRECT -> {
                listOf(Visibility.PUBLIC, Visibility.UNLISTED, Visibility.FOLLOWERS, Visibility.DIRECT)
            }
        }
        return visibilityList
    }

    override suspend fun removeTimelineRelationship(timelineRelationship: TimelineRelationship) {
        removeTimelineObject(timelineRelationship.timelineId, timelineRelationship.actorId)
    }

    override suspend fun updateTimelineRelationship(timelineRelationship: TimelineRelationship) {
        removeTimelineRelationship(timelineRelationship)
        addTimelineRelationship(timelineRelationship)
    }

    override suspend fun addTimeline(timeline: Timeline, timelineRelationshipList: List<TimelineRelationship>) {
        val postList = getPostsByTimelineRelationshipList(timelineRelationshipList)

        val timelineObjectList = postList.mapNotNull { post ->
            val repost = post.repostId?.let { getPost(it) }
            val replyActorId = post.replyId?.let { getPost(it)?.actorId }
            createTimelineObject(post, replyActorId, repost, timeline)
        }

        insertTimelineObject(timelineObjectList)
    }

    override suspend fun removeTimeline(timeline: Timeline) {
        removeTimelineObject(timeline.id)
    }

    protected abstract suspend fun getNextPaging(
        timelineId: TimelineId,
        page: Page?
    ): PaginationList<TimelineObjectDetail, PostId>

    override suspend fun readTimeline(
        timeline: Timeline,
        option: ReadTimelineOption?,
        page: Page?,
        principal: Principal
    ): PaginationList<TimelineObjectDetail, PostId> {
        val timelineObjectList = getTimelineObject(timeline.id, option, page)
        if (timelineObjectList.isEmpty()) {
            return getNextPaging(timeline.id, page)
        }
        val lastUpdatedAt = timelineObjectList.minBy { it.lastUpdatedAt }.lastUpdatedAt

        val newerFilters = getNewerFilters(timeline.userDetailId, lastUpdatedAt)

        val posts =
            getPostsByPostId(
                timelineObjectList.map {
                    it.postId
                } + timelineObjectList.mapNotNull { it.repostId } + timelineObjectList.mapNotNull { it.replyId },
                principal
            )

        val userDetails = getUserDetails(timelineObjectList.map { it.userDetailId })

        val actors =
            getActors(
                timelineObjectList.map { it.postActorId } +
                    timelineObjectList.mapNotNull { it.repostActorId } +
                    timelineObjectList.mapNotNull { it.replyActorId }
            )

        val postMap = posts.associate { post ->
            post.id to applyFilters(post, newerFilters)
        }

        val mediaMap = getMedias(
            posts.flatMap { it.mediaIds } +
                actors.mapNotNull { it.value.icon }
        )

        val reactions = getReactions(posts.map { it.id })

        return PaginationList(
            timelineObjectList.mapNotNull<TimelineObject, TimelineObjectDetail> {
                val timelineUserDetail = userDetails[it.userDetailId] ?: return@mapNotNull null
                val actor = actors[it.postActorId] ?: return@mapNotNull null
                val post = postMap[it.postId] ?: return@mapNotNull null
                val postMedias = post.post.mediaIds.mapNotNull { mediaId -> mediaMap[mediaId] }
                val reply = postMap[it.replyId]
                val replyMedias = reply?.post?.mediaIds?.mapNotNull { mediaId -> mediaMap[mediaId] }
                val replyActor = actors[it.replyActorId]
                val repost = postMap[it.repostId]
                val repostMedias = repost?.post?.mediaIds?.mapNotNull { mediaId -> mediaMap[mediaId] }
                val repostActor = actors[it.repostActorId]
                val reactionsList = reactions[it.postId].orEmpty()
                TimelineObjectDetail.of(
                    timelineObject = it,
                    timelineUserDetail = timelineUserDetail,
                    post = post.post,
                    postMedias = postMedias,
                    postActor = actor,
                    postActorIconMedia = mediaMap[actor.icon],
                    replyPost = reply?.post,
                    replyPostMedias = replyMedias,
                    replyPostActor = replyActor,
                    replyPostActorIconMedia = mediaMap[replyActor?.icon],
                    repostPost = repost?.post,
                    repostPostMedias = repostMedias,
                    repostPostActor = repostActor,
                    repostPostActorIconMedia = mediaMap[repostActor?.icon],
                    warnFilter = it.warnFilters + post.filterResults.map { filterResult ->
                        TimelineObjectWarnFilter(
                            filterResult.filter.id,
                            filterResult.matchedKeyword
                        )
                    },
                    reactionsList = reactionsList,
                    favourited = it.favourited
                )
            },
            timelineObjectList.lastOrNull()?.postId,
            timelineObjectList.firstOrNull()?.postId
        )
    }

    protected abstract suspend fun getActors(actorIds: List<ActorId>): Map<ActorId, Actor>

    protected abstract suspend fun getMedias(mediaIds: List<MediaId>): Map<MediaId, Media>

    protected abstract suspend fun getReactions(postIds: List<PostId>): Map<PostId, List<Reactions>>

    protected abstract suspend fun getUserDetails(userDetailIdList: List<UserDetailId>): Map<UserDetailId, UserDetail>
}
