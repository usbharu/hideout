package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Timeline
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.repository.TimelineRepository
import dev.usbharu.hideout.service.core.IdGenerateService
import org.springframework.stereotype.Service

@Service
class TimelineService(
    private val followerQueryService: FollowerQueryService,
    private val userQueryService: UserQueryService,
    private val timelineRepository: TimelineRepository,
    private val idGenerateService: IdGenerateService
) {
    suspend fun publishTimeline(post: Post, isLocal: Boolean) {
        val findFollowersById = followerQueryService.findFollowersById(post.userId).toMutableList()
        if (isLocal) {
            // 自分自身も含める必要がある
            val user = userQueryService.findById(post.userId)
            findFollowersById.add(user)
        }
        val timelines = findFollowersById.map {
            Timeline(
                id = idGenerateService.generateId(),
                userId = it.id,
                timelineId = 0,
                postId = post.id,
                postUserId = post.userId,
                createdAt = post.createdAt,
                replyId = post.replyId,
                repostId = post.repostId,
                visibility = post.visibility,
                sensitive = post.sensitive,
                isLocal = isLocal
            )
        }.toMutableList()
        if (post.visibility == Visibility.PUBLIC) {
            timelines.add(
                Timeline(
                    id = idGenerateService.generateId(),
                    userId = 0,
                    timelineId = 0,
                    postId = post.id,
                    postUserId = post.userId,
                    createdAt = post.createdAt,
                    replyId = post.replyId,
                    repostId = post.repostId,
                    visibility = post.visibility,
                    sensitive = post.sensitive,
                    isLocal = isLocal
                )
            )
        }
        timelineRepository.saveAll(timelines)
    }
}
