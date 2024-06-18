package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorPublicKey
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.emoji.EmojiId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import utils.AssertDomainEvent.assertContainsEvent
import utils.AssertDomainEvent.assertEmpty
import java.net.URI
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PostTest {
    @Test
    fun deletedがtrueのときghostのidが返される() {
        val post = TestPostFactory.create(deleted = true)

        assertEquals(ActorId.ghost, post.actorId)
    }

    @Test
    fun deletedがfalseの時actorのIDが返される() {
        val post = TestPostFactory.create(deleted = false, actorId = 100)

        assertEquals(ActorId(100), post.actorId)
    }

    @Test
    fun visibilityがDIRECTのとき変更できない() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)

        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))

        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.PUBLIC, actor)
        }
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.UNLISTED, actor)
        }
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.FOLLOWERS, actor)
        }
    }

    @Test
    fun visibilityを小さくすることはできないPUBLIC() {
        val post = TestPostFactory.create(visibility = Visibility.PUBLIC)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.DIRECT, actor)
        }
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.UNLISTED, actor)
        }
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.FOLLOWERS, actor)
        }
    }

    @Test
    fun visibilityを小さくすることはできないUNLISTED() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.DIRECT, actor)
        }
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.FOLLOWERS, actor)
        }
    }

    @Test
    fun visibilityを小さくすることはできないFOLLOWERS() {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.DIRECT, actor)
        }
    }

    @Test
    fun visibilityをDIRECTにあとからすることはできない() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.DIRECT, actor)
        }
    }

    @Test
    fun visibilityを大きくすることができるFOLLOWERS() {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertDoesNotThrow {
            post.setVisibility(Visibility.UNLISTED, actor)
        }

        val post2 = TestPostFactory.create(visibility = Visibility.FOLLOWERS)

        assertDoesNotThrow {
            post2.setVisibility(Visibility.PUBLIC, actor)
        }
    }

    @Test
    fun visibilityを大きくすることができるUNLISTED() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertDoesNotThrow {
            post.setVisibility(Visibility.PUBLIC, actor)
        }
    }

    @Test
    fun deletedがtrueのときvisibilityを変更できない() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED, deleted = true)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setVisibility(Visibility.PUBLIC, actor)
        }
    }

    @Test
    fun visibilityが変更されない限りドメインイベントは発生しない() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setVisibility(Visibility.UNLISTED, actor)
        assertEmpty(post)

    }

    @Test
    fun visibilityが変更されるとupdateイベントが発生する() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setVisibility(Visibility.PUBLIC, actor)

        assertContainsEvent(post, PostEvent.UPDATE.eventName)
    }

    @Test
    fun deletedがtrueのときvisibleActorsを変更できない() {
        val post = TestPostFactory.create(deleted = true)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setVisibleActors(setOf(ActorId(100)), actor)
        }
    }

    @Test
    fun ゔvisibilityがDIRECT以外の時visibleActorsを変更できない() {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setVisibleActors(setOf(ActorId(100)), actor)
        assertEmpty(post)

        val post2 = TestPostFactory.create(visibility = Visibility.UNLISTED)

        post2.setVisibleActors(setOf(ActorId(100)), actor)
        assertEmpty(post2)

        val post3 = TestPostFactory.create(visibility = Visibility.PUBLIC)

        post3.setVisibleActors(setOf(ActorId(100)), actor)
        assertEmpty(post3)
    }

    @Test
    fun visibilityがDIRECTの時visibleActorsを変更できる() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setVisibleActors(setOf(ActorId(100)), actor)
        assertEquals(setOf(ActorId(100)), post.visibleActors)
    }

    @Test
    fun visibleActorsから削除されることはない() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT, visibleActors = listOf(100))
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setVisibleActors(setOf(ActorId(200)), actor)
        assertEquals(setOf(ActorId(100), ActorId(200)), post.visibleActors)
    }

    @Test
    fun visibleActorsに追加された時updateイベントが発生する() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setVisibleActors(setOf(ActorId(100)), actor)

        assertContainsEvent(post, PostEvent.UPDATE.eventName)
    }

    @Test
    fun hideがtrueのときcontentがemptyを返す() {
        val post = TestPostFactory.create(hide = true)

        assertEquals(PostContent.empty, post.content)
    }

    @Test
    fun deletedがtrueの時contentをセットできない() {
        val post = TestPostFactory.create(deleted = true)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setContent(PostContent("test", "test", emptyList()), actor)
        }
    }

    @Test
    fun contentの内容が変更されたらupdateイベントが発生する() {
        val post = TestPostFactory.create()
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setContent(PostContent("test", "test", emptyList()), actor)
        assertContainsEvent(post, PostEvent.UPDATE.eventName)
    }

    @Test
    fun hideがtrueの時nullを返す() {
        val post = TestPostFactory.create(hide = true, overview = "aaaa")

        assertNull(post.overview)
    }

    @Test
    fun hideがfalseの時overviewを返す() {
        val post = TestPostFactory.create(hide = false, overview = "aaaa")

        assertEquals(PostOverview("aaaa"), post.overview)
    }

    @Test
    fun deletedがtrueのときセットできない() {
        val post = TestPostFactory.create(deleted = true)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setOverview(PostOverview("aaaa"), actor)
        }
    }

    @Test
    fun deletedがfalseのときセットできる() {
        val post = TestPostFactory.create(deleted = false)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        val overview = PostOverview("aaaa")
        assertDoesNotThrow {
            post.setOverview(overview, actor)
        }
        assertEquals(overview, post.overview)

        assertContainsEvent(post, PostEvent.UPDATE.eventName)
    }

    @Test
    fun overviewの内容が更新されなかった時イベントが発生しない() {
        val post = TestPostFactory.create(overview = "aaaa")
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setOverview(PostOverview("aaaa"), actor)
        assertEmpty(post)
    }

    @Test
    fun sensitiveが変更されるとupdateイベントが発生する() {
        val post = TestPostFactory.create()
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setSensitive(true, actor)
        assertContainsEvent(post, PostEvent.UPDATE.eventName)
    }

    @Test
    fun 削除されている場合sensitiveを変更できない() {
        val post = TestPostFactory.create(deleted = true)
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        assertThrows<IllegalArgumentException> {
            post.setSensitive(true, actor)
        }
    }

    @Test
    fun sensitiveが変更されなかった場合イベントが発生しない() {
        val post = TestPostFactory.create(overview = "aaaa")
        val actor = TestActorFactory.create(id = post.actorId.id, publicKey = ActorPublicKey(""))
        post.setSensitive(false, actor)
        assertEmpty(post)
    }

    @Test
    fun hideがtrueの時emptyが帰る() {
        val post = TestPostFactory.create(hide = true)

        assertEquals(PostContent.empty.text, post.text)
    }

    @Test
    fun hideがfalseの時textが返る() {
        val post = TestPostFactory.create(hide = false, content = "aaaa")

        assertEquals("aaaa", post.text)
    }

    @Test
    fun `create actorが削除済みの時作成できない`() {
        val actor = TestActorFactory.create(deleted = true)
        assertThrows<IllegalArgumentException> {
            Post.create(
                id = PostId(1),
                actorId = actor.id,
                overview = null,
                content = PostContent.empty,
                createdAt = Instant.now(),
                visibility = Visibility.PUBLIC,
                url = URI.create("https://example.com"),
                repostId = null,
                replyId = null,
                sensitive = false,
                apId = URI.create("https://example.com"),
                deleted = false,
                mediaIds = emptyList(),
                visibleActors = emptySet(),
                hide = false,
                moveTo = null,
                actor = actor

            )
        }
    }

    @Test
    fun `create actorがsuspedの時visibilityがpublicの登校はunlistedになる`() {
        val actor = TestActorFactory.create(suspend = true)

        val post = Post.create(
            id = PostId(1),
            actorId = actor.id,
            overview = null,
            content = PostContent.empty,
            createdAt = Instant.now(),
            visibility = Visibility.PUBLIC,
            url = URI.create("https://example.com"),
            repostId = null,
            replyId = null,
            sensitive = false,
            apId = URI.create("https://example.com"),
            deleted = false,
            mediaIds = emptyList(),
            visibleActors = emptySet(),
            hide = false,
            moveTo = null,
            actor = actor
        )

        assertEquals(Visibility.UNLISTED, post.visibility)
    }

    @Test
    fun `create actorがsuspedの時visibilityがunlistedの登校は変わらない`() {
        val actor = TestActorFactory.create(suspend = true)

        val post = Post.create(
            id = PostId(1),
            actorId = actor.id,
            overview = null,
            content = PostContent.empty,
            createdAt = Instant.now(),
            visibility = Visibility.UNLISTED,
            url = URI.create("https://example.com"),
            repostId = null,
            replyId = null,
            sensitive = false,
            apId = URI.create("https://example.com"),
            deleted = false,
            mediaIds = emptyList(),
            visibleActors = emptySet(),
            hide = false,
            moveTo = null,
            actor = actor
        )

        assertEquals(Visibility.UNLISTED, post.visibility)
    }

    @Test
    fun `create 作成できる`() {
        val actor = TestActorFactory.create(suspend = true)


        assertDoesNotThrow {

            Post.create(
                id = PostId(1),
                actorId = actor.id,
                overview = null,
                content = PostContent.empty,
                createdAt = Instant.now(),
                visibility = Visibility.PUBLIC,
                url = URI.create("https://example.com"),
                repostId = null,
                replyId = null,
                sensitive = false,
                apId = URI.create("https://example.com"),
                deleted = false,
                mediaIds = emptyList(),
                visibleActors = emptySet(),
                hide = false,
                moveTo = null,
                actor = actor
            )
        }
    }

    @Test
    fun `create 作成できる2`() {
        val actor = TestActorFactory.create(suspend = true)


        assertDoesNotThrow {

            Post.create(
                id = PostId(1),
                actorId = actor.id,
                content = PostContent.empty,
                createdAt = Instant.now(),
                visibility = Visibility.PUBLIC,
                url = URI.create("https://example.com"),
                repostId = null,
                replyId = null,
                sensitive = false,
                apId = URI.create("https://example.com"),
                deleted = false,
                mediaIds = emptyList(),
                actor = actor
            )
        }
    }

    @Test
    fun `emojiIds hideがtrueの時empty`() {
        val actor = TestActorFactory.create()
        val emojiIds = listOf(EmojiId(1), EmojiId(2))
        val post = Post.create(
            id = PostId(1),
            actorId = actor.id,
            content = PostContent("aaa", "aaa", emojiIds),
            createdAt = Instant.now(),
            visibility = Visibility.PUBLIC,
            url = URI.create("https://example.com"),
            repostId = null,
            replyId = null,
            sensitive = false,
            apId = URI.create("https://example.com"),
            deleted = false,
            mediaIds = emptyList(),
            actor = actor,
            hide = true
        )

        assertEquals(PostContent.empty.emojiIds, post.emojiIds)

    }

    @Test
    fun `emojiIds hideがfalseの時中身が返される`() {
        val actor = TestActorFactory.create()
        val emojiIds = listOf(EmojiId(1), EmojiId(2))
        val post = Post.create(
            id = PostId(1),
            actorId = actor.id,
            content = PostContent("aaa", "aaa", emojiIds),
            createdAt = Instant.now(),
            visibility = Visibility.PUBLIC,
            url = URI.create("https://example.com"),
            repostId = null,
            replyId = null,
            sensitive = false,
            apId = URI.create("https://example.com"),
            deleted = false,
            mediaIds = emptyList(),
            actor = actor,
            hide = false
        )

        assertEquals(emojiIds, post.emojiIds)
    }

    @Test
    fun `reconstructWith 与えた引数で上書きされる`() {
        val post = TestPostFactory.create()
        val mediaIds = listOf<MediaId>(MediaId(1))
        val visibleActors = setOf<ActorId>((ActorId(2)))
        val emojis = listOf<EmojiId>(EmojiId(3))
        val reconstructWith = post.reconstructWith(mediaIds, emojis, visibleActors)

        assertEquals(mediaIds, reconstructWith.mediaIds)
        assertEquals(visibleActors, reconstructWith.visibleActors)
        assertEquals(emojis, reconstructWith.emojiIds)
    }

    @Test
    fun `mediaIds hideがtrueの時emptyが返される`() {
        val actor = TestActorFactory.create()
        val emojiIds = listOf(EmojiId(1), EmojiId(2))
        val mediaIds = listOf(MediaId(1))
        val post = Post.create(
            id = PostId(1),
            actorId = actor.id,
            content = PostContent("aaa", "aaa", emojiIds),
            createdAt = Instant.now(),
            visibility = Visibility.PUBLIC,
            url = URI.create("https://example.com"),
            repostId = null,
            replyId = null,
            sensitive = false,
            apId = URI.create("https://example.com"),
            deleted = false,
            mediaIds = mediaIds,
            actor = actor,
            hide = true
        )

        assertEquals(emptyList(), post.mediaIds)

    }

    @Test
    fun `mediaIds hideがfalseの時中身が返される`() {
        val actor = TestActorFactory.create()
        val emojiIds = listOf(EmojiId(1), EmojiId(2))
        val mediaIds = listOf(MediaId(2))
        val post = Post.create(
            id = PostId(1),
            actorId = actor.id,
            content = PostContent("aaa", "aaa", emojiIds),
            createdAt = Instant.now(),
            visibility = Visibility.PUBLIC,
            url = URI.create("https://example.com"),
            repostId = null,
            replyId = null,
            sensitive = false,
            apId = URI.create("https://example.com"),
            deleted = false,
            mediaIds = mediaIds,
            actor = actor,
            hide = false
        )

        assertEquals(mediaIds, post.mediaIds)
    }
}
