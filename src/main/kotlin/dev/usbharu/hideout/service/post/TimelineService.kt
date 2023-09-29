package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Timeline
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.repository.TimelineRepository
import org.springframework.stereotype.Service

@Service
class TimelineService(
    private val followerQueryService: FollowerQueryService,
    private val timelineRepository: TimelineRepository
) {
    suspend fun publishTimeline(post: Post, isLocal: Boolean) {
        val findFollowersById = followerQueryService.findFollowersById(post.userId)
        timelineRepository.saveAll(findFollowersById.map {
            Timeline(
                id = timelineRepository.generateId(),
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
        })
    }
}
