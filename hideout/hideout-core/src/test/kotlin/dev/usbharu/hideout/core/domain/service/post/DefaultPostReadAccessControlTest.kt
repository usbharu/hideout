package dev.usbharu.hideout.core.domain.service.post

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.TestPostFactory
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class DefaultPostReadAccessControlTest {
    @InjectMocks
    lateinit var service: DefaultPostReadAccessControl

    @Mock
    lateinit var relationshipRepository: RelationshipRepository

    @Test
    fun ブロックされてたら見れない() = runTest {
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(1), ActorId(2))).doReturn(
            Relationship(
                actorId = ActorId(1),
                targetActorId = ActorId(2),
                following = false,
                blocking = true,
                muting = false,
                followRequesting = false,
                mutingFollowRequest = false,
            )
        )

        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1),
            LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertFalse(actual)
    }

    @Test
    fun PublicかUnlistedなら見れる() = runTest {
        val actual = service.isAllow(TestPostFactory.create(visibility = Visibility.PUBLIC), Anonymous)
        assertTrue(actual)

        val actual2 = service.isAllow(TestPostFactory.create(visibility = Visibility.UNLISTED), Anonymous)
        assertTrue(actual2)
    }

    @Test
    fun FollowersかDirecのときAnonymousなら見れない() = runTest {
        val actual = service.isAllow(TestPostFactory.create(visibility = Visibility.FOLLOWERS), Anonymous)
        assertFalse(actual)

        val actual2 = service.isAllow(TestPostFactory.create(visibility = Visibility.DIRECT), Anonymous)
        assertFalse(actual2)
    }

    @Test
    fun DirectでvisibleActorsに含まれていたら見れる() = runTest {
        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.DIRECT, visibleActors = listOf(2)),
            LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertTrue(actual)
    }

    @Test
    fun DirectでvisibleActorsに含まれていなかったら見れない() = runTest {
        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.DIRECT, visibleActors = listOf(3)),
            LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertFalse(actual)
    }

    @Test
    fun Followersでフォロワーなら見れる() = runTest {
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(1), ActorId(2))).doReturn(
            Relationship.default(
                actorId = ActorId(1),
                targetActorId = ActorId(2)
            )
        )
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(2), ActorId(1))).doReturn(
            Relationship(
                actorId = ActorId(2),
                targetActorId = ActorId(1),
                following = true,
                blocking = false,
                muting = false,
                followRequesting = false,
                mutingFollowRequest = false
            )
        )


        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.FOLLOWERS),
            LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertTrue(actual)
    }

    @Test
    fun relationshipが見つからない場合見れない() = runTest {
        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.FOLLOWERS),
            LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertFalse(actual)
    }

    @Test
    fun フォロワーじゃない場合は見れない() = runTest {
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(1), ActorId(2))).doReturn(
            Relationship.default(
                actorId = ActorId(1),
                targetActorId = ActorId(2)
            )
        )
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(2), ActorId(1))).doReturn(
            Relationship(
                actorId = ActorId(2),
                targetActorId = ActorId(1),
                following = false,
                blocking = false,
                muting = false,
                followRequesting = false,
                mutingFollowRequest = false
            )
        )


        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.FOLLOWERS),
            LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertFalse(actual)
    }

    @Test
    fun ポスト主は無条件で見れる() = runTest {
        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.DIRECT),
            LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com"))
        )

        assertTrue(actual)
    }

    @Test
    fun areAllows_ポスト主は無条件で見れる() = runTest {
        whenever(
            relationshipRepository.findByActorIdsAndTargetIdAndBlocking(
                any(),
                anyValueClass(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )
        whenever(
            relationshipRepository.findByActorIdAndTargetIdsAndFollowing(
                anyValueClass(),
                any(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )

        val postList = listOf<Post>(
            TestPostFactory.create(actorId = 1, visibility = Visibility.DIRECT),
            TestPostFactory.create(actorId = 1, visibility = Visibility.FOLLOWERS),
            TestPostFactory.create(actorId = 1, visibility = Visibility.UNLISTED),
            TestPostFactory.create(actorId = 1, visibility = Visibility.PUBLIC),
        )
        val actual = service.areAllows(postList, LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com")))

        assertContentEquals(postList, actual)
    }

    @Test
    fun areFollows_ブロックされていたら見れない() = runTest {
        whenever(
            relationshipRepository.findByActorIdsAndTargetIdAndBlocking(
                any(),
                anyValueClass(),
                eq(true)
            )
        ).doReturn(
            listOf(Relationship.default(actorId = ActorId(2), targetActorId = ActorId(1)))
        )
        whenever(
            relationshipRepository.findByActorIdAndTargetIdsAndFollowing(
                anyValueClass(),
                any(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )

        val postList = listOf<Post>(
            TestPostFactory.create(actorId = 1, visibility = Visibility.DIRECT),
            TestPostFactory.create(actorId = 2, visibility = Visibility.FOLLOWERS),
            TestPostFactory.create(actorId = 1, visibility = Visibility.UNLISTED),
            TestPostFactory.create(actorId = 1, visibility = Visibility.PUBLIC),
        )
        val actual = service.areAllows(postList, LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com")))

        assertEquals(3, actual.size)
        assertAll(actual.map { { assertEquals(1, it.actorId.id) } })
    }


    @Test
    fun areAllows_PUBLICとUNLISTEDは見れる() = runTest {
        whenever(
            relationshipRepository.findByActorIdsAndTargetIdAndBlocking(
                any(),
                anyValueClass(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )
        whenever(
            relationshipRepository.findByActorIdAndTargetIdsAndFollowing(
                anyValueClass(),
                any(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )

        val postList = listOf<Post>(
            TestPostFactory.create(actorId = 3, visibility = Visibility.DIRECT),
            TestPostFactory.create(actorId = 3, visibility = Visibility.FOLLOWERS),
            TestPostFactory.create(actorId = 3, visibility = Visibility.UNLISTED),
            TestPostFactory.create(actorId = 3, visibility = Visibility.PUBLIC),
        )
        val actual = service.areAllows(postList, LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com")))

        assertEquals(2, actual.size)
        kotlin.test.assertTrue(actual.all { it.visibility == Visibility.PUBLIC || it.visibility == Visibility.UNLISTED })
    }

    @Test
    fun areAllows_Anonymousは見れない() = runTest {
        whenever(
            relationshipRepository.findByActorIdsAndTargetIdAndBlocking(
                any(),
                anyValueClass(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )
        whenever(
            relationshipRepository.findByActorIdAndTargetIdsAndFollowing(
                anyValueClass(),
                any(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )

        val postList = listOf<Post>(
            TestPostFactory.create(actorId = 3, visibility = Visibility.DIRECT),
            TestPostFactory.create(actorId = 3, visibility = Visibility.FOLLOWERS),
            TestPostFactory.create(actorId = 3, visibility = Visibility.UNLISTED),
            TestPostFactory.create(actorId = 3, visibility = Visibility.PUBLIC),
        )
        val actual = service.areAllows(postList, Anonymous)

        assertEquals(2, actual.size)
        kotlin.test.assertTrue(actual.all { it.visibility == Visibility.PUBLIC || it.visibility == Visibility.UNLISTED })
    }

    @Test
    fun areAllows_DIRECTはVisibleActorsに入っていたら見れる() = runTest {
        whenever(
            relationshipRepository.findByActorIdsAndTargetIdAndBlocking(
                any(),
                anyValueClass(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )
        whenever(
            relationshipRepository.findByActorIdAndTargetIdsAndFollowing(
                anyValueClass(),
                any(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )

        val postList = listOf<Post>(
            TestPostFactory.create(id = 1, actorId = 3, visibility = Visibility.DIRECT, visibleActors = listOf(1)),
            TestPostFactory.create(id = 2, actorId = 3, visibility = Visibility.DIRECT, visibleActors = listOf(2)),
            TestPostFactory.create(id = 3, actorId = 3, visibility = Visibility.DIRECT, visibleActors = listOf(3)),
            TestPostFactory.create(
                id = 4,
                actorId = 3,
                visibility = Visibility.DIRECT,
                visibleActors = listOf(1, 2, 3, 4)
            ),
        )
        val actual = service.areAllows(postList, LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com")))

        assertEquals(2, actual.size)
        kotlin.test.assertTrue(actual.all { it.id.id == 1L || it.id.id == 4L })
    }

    @Test
    fun areAllows_FOLLOWERSはフォローされていたら見れる() = runTest {
        whenever(
            relationshipRepository.findByActorIdsAndTargetIdAndBlocking(
                any(),
                anyValueClass(),
                eq(true)
            )
        ).doReturn(
            emptyList()
        )
        whenever(
            relationshipRepository.findByActorIdAndTargetIdsAndFollowing(
                anyValueClass(),
                any(),
                eq(true)
            )
        ).doReturn(
            listOf(Relationship.default(actorId = ActorId(1), targetActorId = ActorId(2)))
        )

        val postList = listOf<Post>(
            TestPostFactory.create(actorId = 3, visibility = Visibility.FOLLOWERS),
            TestPostFactory.create(actorId = 2, visibility = Visibility.FOLLOWERS),
            TestPostFactory.create(actorId = 3, visibility = Visibility.FOLLOWERS),
            TestPostFactory.create(actorId = 3, visibility = Visibility.FOLLOWERS),
        )
        val actual = service.areAllows(postList, LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com")))

        assertEquals(1, actual.size)
        assertAll(actual.map { { assertEquals(2, it.actorId.id) } })
    }
}