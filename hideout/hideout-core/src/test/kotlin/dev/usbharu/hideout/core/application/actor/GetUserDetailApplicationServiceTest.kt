package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailHashedPassword
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class GetUserDetailApplicationServiceTest {
    @InjectMocks
    lateinit var service: GetUserDetailApplicationService

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var userDetailRepository: UserDetailRepository

    @Mock
    lateinit var customEmojiRepository: CustomEmojiRepository

    @Spy
    val transaction = TestTransaction

    @Test
    fun userDetailを取得できる() = runTest {
        whenever(userDetailRepository.findById(UserDetailId(1))).doReturn(
            UserDetail.create(
                UserDetailId(1), ActorId(1),
                UserDetailHashedPassword("")
            )
        )
        whenever(actorRepository.findById(ActorId(1))).doReturn(TestActorFactory.create(1))
        whenever(customEmojiRepository.findByIds(any())).doReturn(listOf())

        service.execute(GetUserDetail(1), Anonymous)
    }

    @Test
    fun userDetailが存在しない場合失敗() = runTest {

        assertThrows<IllegalArgumentException> {
            service.execute(GetUserDetail(2), Anonymous)
        }
    }

    @Test
    fun userDetailが存在するけどActorが存在しない場合はInternalServerException() = runTest {
        whenever(userDetailRepository.findById(UserDetailId(2))).doReturn(
            UserDetail.create(
                UserDetailId(2), ActorId(2),
                UserDetailHashedPassword("")
            )
        )

        assertThrows<InternalServerException> {
            service.execute(GetUserDetail(2), Anonymous)
        }
    }
}