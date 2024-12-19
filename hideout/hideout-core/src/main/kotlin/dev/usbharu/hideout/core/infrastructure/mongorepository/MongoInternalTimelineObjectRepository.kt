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

package dev.usbharu.hideout.core.infrastructure.mongorepository

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.filter.FilterId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectWarnFilter
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.timeline.InternalTimelineObjectOption
import dev.usbharu.hideout.core.infrastructure.timeline.InternalTimelineObjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MongoInternalTimelineObjectRepository(
    private val springDataMongoTimelineObjectRepository: SpringDataMongoTimelineObjectRepository,
    private val mongoTemplate: MongoTemplate
) :
    InternalTimelineObjectRepository {
    override suspend fun save(timelineObject: TimelineObject): TimelineObject {
        springDataMongoTimelineObjectRepository.save(SpringDataMongoTimelineObject.of(timelineObject))
        return timelineObject
    }

    override suspend fun saveAll(timelineObjectList: List<TimelineObject>): List<TimelineObject> {
        springDataMongoTimelineObjectRepository.saveAll(timelineObjectList.map { SpringDataMongoTimelineObject.of(it) })
            .collect()
        return timelineObjectList
    }

    override suspend fun findByPostId(postId: PostId): List<TimelineObject> =
        springDataMongoTimelineObjectRepository.findByPostId(postId.id).map { it.toTimelineObject() }.toList()

    override suspend fun deleteByPostId(postId: PostId) {
        springDataMongoTimelineObjectRepository.deleteByPostId(postId.id)
    }

    override suspend fun deleteByTimelineIdAndActorId(timelineId: TimelineId, actorId: ActorId) {
        springDataMongoTimelineObjectRepository.deleteByTimelineIdAndPostActorId(timelineId.value, actorId.id)
    }

    override suspend fun deleteByTimelineId(timelineId: TimelineId) {
        springDataMongoTimelineObjectRepository.deleteByTimelineId(timelineId.value)
    }

    override suspend fun findByTimelineIdAndPostIdGT(timelineId: TimelineId, postId: PostId): TimelineObject? {
        return springDataMongoTimelineObjectRepository.findFirstByTimelineIdAndPostIdGreaterThanOrderByIdAsc(
            timelineId.value,
            postId.id
        )
            ?.toTimelineObject()
    }

    override suspend fun findByTimelineIdAndPostIdLT(timelineId: TimelineId, postId: PostId): TimelineObject? {
        return springDataMongoTimelineObjectRepository.findFirstByTimelineIdAndPostIdLessThanOrderByIdDesc(
            timelineId.value,
            postId.id
        )
            ?.toTimelineObject()
    }

    override suspend fun findByTimelineId(
        timelineId: TimelineId,
        internalTimelineObjectOption: InternalTimelineObjectOption?,
        page: Page?
    ): PaginationList<TimelineObject, PostId> {
        val query = Query()

        query.addCriteria(Criteria.where("timelineId").isEqualTo(timelineId.value))

        if (page?.minId != null) {
            query.with(Sort.by(Sort.Direction.ASC, "postCreatedAt"))
            page.minId?.let { query.addCriteria(Criteria.where("postId").gt(it)) }
            page.maxId?.let { query.addCriteria(Criteria.where("postId").lt(it)) }
        } else {
            query.with(Sort.by(Sort.Direction.DESC, "postCreatedAt"))
            page?.sinceId?.let { query.addCriteria(Criteria.where("postId").gt(it)) }
            page?.maxId?.let { query.addCriteria(Criteria.where("postId").lt(it)) }
        }

        page?.limit?.let { query.limit(it) }

        val timelineObjects =
            mongoTemplate.find(query, SpringDataMongoTimelineObject::class.java).map { it.toTimelineObject() }

        val objectList = if (page?.minId != null) {
            timelineObjects.reversed()
        } else {
            timelineObjects
        }

        return PaginationList(
            objectList,
            objectList.lastOrNull()?.postId,
            objectList.firstOrNull()?.postId
        )
    }
}

@Document
data class SpringDataMongoTimelineObject(
    @Id
    val id: Long,
    val userDetailId: Long,
    val timelineId: Long,
    val postId: Long,
    val postActorId: Long,
    val postCreatedAt: Long,
    val replyId: Long?,
    val replyActorId: Long?,
    val repostId: Long?,
    val repostActorId: Long?,
    val visibility: Visibility,
    val isPureRepost: Boolean,
    val mediaIds: List<Long>,
    val emojiIds: List<Long>,
    val visibleActors: List<Long>,
    val hasMediaInRepost: Boolean,
    val lastUpdatedAt: Long,
    val warnFilters: List<SpringDataMongoTimelineObjectWarnFilter>,
    val favourited: Boolean
) {

    fun toTimelineObject(): TimelineObject {
        return TimelineObject(
            id = TimelineObjectId(id),
            userDetailId = UserDetailId(userDetailId),
            timelineId = TimelineId(timelineId),
            postId = PostId(postId),
            postActorId = ActorId(postActorId),
            postCreatedAt = Instant.ofEpochSecond(postCreatedAt),
            replyId = replyId?.let { PostId(it) },
            replyActorId = replyActorId?.let { ActorId(it) },
            repostId = repostId?.let { PostId(it) },
            repostActorId = repostActorId?.let { ActorId(it) },
            visibility = visibility,
            isPureRepost = isPureRepost,
            mediaIds = mediaIds.map { MediaId(it) },
            emojiIds = emojiIds.map { CustomEmojiId(it) },
            visibleActors = visibleActors.map { ActorId(it) },
            hasMediaInRepost = hasMediaInRepost,
            lastUpdatedAt = Instant.ofEpochSecond(lastUpdatedAt),
            warnFilters = warnFilters.map { it.toTimelineObjectWarnFilter() },
            favourited = favourited
        )
    }

    companion object {
        fun of(timelineObject: TimelineObject): SpringDataMongoTimelineObject {
            return SpringDataMongoTimelineObject(
                id = timelineObject.id.value,
                userDetailId = timelineObject.userDetailId.id,
                timelineId = timelineObject.timelineId.value,
                postId = timelineObject.postId.id,
                postActorId = timelineObject.postActorId.id,
                postCreatedAt = timelineObject.postCreatedAt.epochSecond,
                replyId = timelineObject.replyId?.id,
                replyActorId = timelineObject.replyActorId?.id,
                repostId = timelineObject.repostId?.id,
                repostActorId = timelineObject.repostActorId?.id,
                visibility = timelineObject.visibility,
                isPureRepost = timelineObject.isPureRepost,
                mediaIds = timelineObject.mediaIds.map { it.id },
                emojiIds = timelineObject.emojiIds.map { it.emojiId },
                visibleActors = timelineObject.visibleActors.map { it.id },
                hasMediaInRepost = timelineObject.hasMediaInRepost,
                lastUpdatedAt = timelineObject.lastUpdatedAt.epochSecond,
                warnFilters = timelineObject.warnFilters.map { SpringDataMongoTimelineObjectWarnFilter.of(it) },
                favourited = timelineObject.favourited
            )
        }
    }
}

data class SpringDataMongoTimelineObjectWarnFilter(
    val filterId: Long,
    val matchedKeyword: String
) {

    fun toTimelineObjectWarnFilter(): TimelineObjectWarnFilter {
        return TimelineObjectWarnFilter(
            FilterId(filterId),
            matchedKeyword
        )
    }

    companion object {
        fun of(timelineObjectWarnFilter: TimelineObjectWarnFilter): SpringDataMongoTimelineObjectWarnFilter {
            return SpringDataMongoTimelineObjectWarnFilter(
                timelineObjectWarnFilter.filterId.id,
                timelineObjectWarnFilter.matchedKeyword
            )
        }
    }
}

@Suppress("FunctionMaxLength")
interface SpringDataMongoTimelineObjectRepository : CoroutineCrudRepository<SpringDataMongoTimelineObject, Long> {
    fun findByPostId(postId: Long): Flow<SpringDataMongoTimelineObject>

    suspend fun deleteByPostId(postId: Long)

    suspend fun deleteByTimelineIdAndPostActorId(timelineId: Long, postActorId: Long)

    suspend fun deleteByTimelineId(timelineId: Long)

    suspend fun findFirstByTimelineIdAndPostIdGreaterThanOrderByIdAsc(
        timelineId: Long,
        postId: Long
    ): SpringDataMongoTimelineObject?

    suspend fun findFirstByTimelineIdAndPostIdLessThanOrderByIdDesc(
        timelineId: Long,
        postId: Long
    ): SpringDataMongoTimelineObject?
}
