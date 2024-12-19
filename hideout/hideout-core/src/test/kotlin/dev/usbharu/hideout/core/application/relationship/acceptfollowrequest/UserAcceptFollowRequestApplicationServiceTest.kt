package dev.usbharu.hideout.core.application.relationship.acceptfollowrequest

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.relationship.AcceptFollowRequest
import dev.usbharu.hideout.core.application.relationship.UserAcceptFollowRequestApplicationService
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class UserAcceptFollowRequestApplicationServiceTest {
    @InjectMocks
    lateinit var service: UserAcceptFollowRequestApplicationService

    @Mock
    lateinit var relationshipRepository: RelationshipRepository

    @Mock
    lateinit var actorRepository: ActorRepository

    @Spy
    val transaction = TestTransaction

    @Test
    fun actorが見つからない場合失敗() = runTest {
        assertThrows<InternalServerException> {
            service.execute(AcceptFollowRequest(1), LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com")))
        }
    }

    @Test
    fun relationshipが見つからない場合失敗() = runTest {
        whenever(actorRepository.findById(ActorId(2))).doReturn(TestActorFactory.create(id = 2))

        assertThrows<InternalServerException> {
            service.execute(AcceptFollowRequest(1), LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com")))
        }
    }

    @Test
    fun フォローリクエストを承認できる() = runTest {
        whenever(actorRepository.findById(ActorId(2))).doReturn(TestActorFactory.create(id = 2))
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(1), ActorId(2))).doReturn(
            Relationship(
                actorId = ActorId(1), targetActorId = ActorId
                    (2),
                following = false,
                blocking = false,
                muting = false,
                followRequesting = true, mutingFollowRequest = false
            )
        )
        service.execute(AcceptFollowRequest(1), LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com")))

        argumentCaptor<Relationship> {
            verify(relationshipRepository).save(capture())
            val first = allValues.first()

            assertFalse(first.followRequesting)
            assertTrue(first.following)
        }
    }
}