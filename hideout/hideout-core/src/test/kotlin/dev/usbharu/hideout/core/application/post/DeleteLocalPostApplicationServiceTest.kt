package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.TestPostFactory
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class DeleteLocalPostApplicationServiceTest {
    @InjectMocks
    lateinit var service: DeleteLocalPostApplicationService

    @Mock
    lateinit var postRepository: PostRepository

    @Mock
    lateinit var actorRepository: ActorRepository

    @Spy
    val transaction = TestTransaction

    @Test
    fun Post主はローカルPostを削除できる() = runTest {
        whenever(postRepository.findById(PostId(1))).doReturn(TestPostFactory.create(actorId = 2))
        whenever(actorRepository.findById(ActorId(2))).doReturn(TestActorFactory.create(id = 2))

        service.execute(DeleteLocalPost(1), LocalUser(ActorId(2), UserDetailId(2), Acct("test", "example.com")))
    }

    @Test
    fun Post主以外はローカルPostを削除できない() = runTest {
        whenever(postRepository.findById(PostId(1))).doReturn(TestPostFactory.create(actorId = 2))

        assertThrows<PermissionDeniedException> {
            service.execute(DeleteLocalPost(1), LocalUser(ActorId(3), UserDetailId(3), Acct("test", "example.com")))
        }
    }
}