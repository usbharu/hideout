package dev.usbharu.hideout.core.domain.model.actor

import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.support.domain.Domain
import dev.usbharu.hideout.core.infrastructure.other.TwitterSnowflakeIdGenerateService
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.time.Instant

object TestActorFactory {
    private val idGenerateService = TwitterSnowflakeIdGenerateService

    fun create(
        id: Long = generateId(),
        actorName: String = "test-$id",
        domain: String = "example.com",
        actorScreenName: String = actorName,
        description: String = "test description",
        inbox: URI = URI.create("https://example.com/$id/inbox"),
        outbox: URI = URI.create("https://example.com/$id/outbox"),
        uri: URI = URI.create("https://example.com/$id"),
        publicKey: ActorPublicKey = ActorPublicKey(""),
        privateKey: ActorPrivateKey? = null,
        createdAt: Instant = Instant.now(),
        keyId: String = "https://example.com/$id#key-id",
        followersEndpoint: URI = URI.create("https://example.com/$id/followers"),
        followingEndpoint: URI = URI.create("https://example.com/$id/following"),
        instanceId: Long = 1L,
        locked: Boolean = false,
        followersCount: Int = 0,
        followingCount: Int = 0,
        postCount: Int = 0,
        lastPostDate: Instant? = null,
        lastUpdateAt: Instant = createdAt,
        suspend: Boolean = false,
        alsoKnownAs: Set<ActorId> = emptySet(),
        moveTo: Long? = null,
        emojiIds: Set<CustomEmojiId> = emptySet(),
        deleted: Boolean = false,
        icon: Long? = null,
        banner: Long? = null,
    ): Actor {
        return runBlocking {
            Actor(
                id = ActorId(id),
                name = ActorName(actorName),
                domain = Domain(domain),
                screenName = ActorScreenName(actorScreenName),
                description = ActorDescription(description),
                inbox = inbox,
                outbox = outbox,
                url = uri,
                publicKey = publicKey,
                privateKey = privateKey,
                createdAt = createdAt,
                keyId = ActorKeyId(keyId),
                followersEndpoint = followersEndpoint,
                followingEndpoint = followingEndpoint,
                instance = InstanceId(instanceId),
                locked = locked,
                followersCount = ActorRelationshipCount(followersCount),
                followingCount = ActorRelationshipCount(followingCount),
                postsCount = ActorPostsCount(postCount),
                lastPostAt = lastPostDate,
                lastUpdateAt = lastUpdateAt,
                suspend = suspend,
                alsoKnownAs = alsoKnownAs,
                moveTo = moveTo?.let { ActorId(it) },
                emojiIds = emojiIds,
                deleted = deleted,
                icon = icon?.let { MediaId(it) },
                banner = banner?.let { MediaId(it) },

                )
        }
    }

    private fun generateId(): Long = runBlocking {
        idGenerateService.generateId()
    }
}
