package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.TestPostFactory
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.factory.PostFactoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class RegisterLocalPostApplicationServiceTest {
    @InjectMocks
    lateinit var service: RegisterLocalPostApplicationService

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var postRepository: PostRepository

    @Mock
    lateinit var postFactoryImpl: PostFactoryImpl

    @Spy
    val transaction = TestTransaction

    @Test
    fun postを作成できる() = runTest {
        val actor = TestActorFactory.create(id = 1)
        val post = TestPostFactory.create()
        whenever(actorRepository.findById(ActorId(1))) doReturn actor
        whenever(
            postFactoryImpl.createLocal(
                eq(actor),
                anyValueClass(),
                isNull(),
                eq("content test"),
                eq(Visibility.PUBLIC),
                isNull(),
                isNull(),
                eq(false),
                eq(emptyList())
            )
        ).doReturn(post)

        service.execute(
            RegisterLocalPost("content test", null, Visibility.PUBLIC, null, null, false, emptyList()), LocalUser(
                ActorId(1), UserDetailId(1), Acct("test", "example.com")
            )
        )

        verify(postRepository, times(1)).save(eq(post))
    }

    @Test
    fun actorが見つからないと失敗() = runTest {
        assertThrows<InternalServerException> {
            service.execute(
                RegisterLocalPost("content test", null, Visibility.PUBLIC, null, null, false, emptyList()), LocalUser(
                    ActorId(1), UserDetailId(1), Acct("test", "example.com")
                )
            )
        }
    }
}