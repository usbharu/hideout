package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
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
class StartDeleteLocalActorApplicationServiceTest {

    @InjectMocks
    lateinit var service: StartDeleteLocalActorApplicationService

    @Mock
    lateinit var actorRepository: ActorRepository

    @Spy
    val transaction = TestTransaction

    @Test
    fun ローカルActorを削除できる() = runTest {
        whenever(actorRepository.findById(ActorId(1))).doReturn(TestActorFactory.create(1))

        service.execute(
            DeleteLocalActor(ActorId(1)),
            LocalUser(ActorId(1), UserDetailId((1)), Acct("test", "example.com"))
        )
    }

    @Test
    fun ログイン中のユーザーと一致しない場合失敗() = runTest {
        assertThrows<PermissionDeniedException> {
            service.execute(
                DeleteLocalActor(ActorId(2)),
                LocalUser(ActorId(1), UserDetailId((1)), Acct("test", "example.com"))
            )
        }
    }

    @Test
    fun ユーザーが存在しない場合失敗() = runTest {
        assertThrows<InternalServerException> {
            service.execute(
                DeleteLocalActor(ActorId(1)),
                LocalUser(ActorId(1), UserDetailId((1)), Acct("test", "example.com"))
            )
        }
    }
}