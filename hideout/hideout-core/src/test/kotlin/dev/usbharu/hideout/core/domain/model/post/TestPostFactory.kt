package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.infrastructure.other.TwitterSnowflakeIdGenerateService
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.time.Instant

object TestPostFactory {
    private val idGenerateService = TwitterSnowflakeIdGenerateService

    fun create(
        id: Long = generateId(),
        actorId: Long = 1,
        instanceId: Long = 1,
        overview: String? = null,
        content: String = "This is test content",
        createdAt: Instant = Instant.now(),
        visibility: Visibility = Visibility.PUBLIC,
        url: URI = URI.create("https://example.com/$actorId/posts/$id"),
        repostId: Long? = null,
        replyId: Long? = null,
        sensitive: Boolean = false,
        apId: URI = URI.create("https://example.com/$actorId/posts/$id"),
        deleted: Boolean = false,
        mediaIds: List<Long> = emptyList(),
        visibleActors: List<Long> = emptyList(),
        hide: Boolean = false,
        moveTo: Long? = null,
        emojiIds: List<Long> = emptyList(),
    ): Post {
        return Post(
            PostId(id),
            ActorId(actorId),
            instanceId = InstanceId(instanceId),
            overview = overview?.let { PostOverview(it) },
            content = PostContent(content, content, emojiIds.map { CustomEmojiId(it) }),
            createdAt = createdAt,
            visibility = visibility,
            url = url,
            repostId = repostId?.let { PostId(it) },
            replyId = replyId?.let { PostId(it) },
            sensitive = sensitive,
            apId = apId,
            deleted = deleted,
            mediaIds = mediaIds.map { MediaId(it) },
            visibleActors = visibleActors.map { ActorId(it) }.toSet(),
            hide = hide,
            moveTo = moveTo?.let { PostId(it) }
        )
    }

    private fun generateId(): Long = runBlocking {
        idGenerateService.generateId()
    }
}