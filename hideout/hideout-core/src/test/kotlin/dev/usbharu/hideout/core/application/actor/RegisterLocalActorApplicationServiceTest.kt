package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.service.actor.local.LocalActorDomainService
import dev.usbharu.hideout.core.domain.service.userdetail.UserDetailDomainService
import dev.usbharu.hideout.core.infrastructure.factory.ActorFactoryImpl
import dev.usbharu.hideout.core.infrastructure.other.TwitterSnowflakeIdGenerateService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import utils.TestTransaction
import java.net.URI

@ExtendWith(MockitoExtension::class)
class RegisterLocalActorApplicationServiceTest {
    @InjectMocks
    lateinit var service: RegisterLocalActorApplicationService

    @Mock
    lateinit var actorDomainService: LocalActorDomainService

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var actorFactoryImpl: ActorFactoryImpl

    @Mock
    lateinit var instanceRepository: InstanceRepository

    @Mock
    lateinit var userDetailDomainService: UserDetailDomainService

    @Mock
    lateinit var userDetailRepository: UserDetailRepository

    @Spy
    val transaction = TestTransaction

    @Spy
    val applicationConfig = ApplicationConfig(URI.create("http://example.com"))

    @Spy
    val idGenerateService = TwitterSnowflakeIdGenerateService

    @Test
    fun usernameがすでに使われていた場合失敗() = runTest {
        whenever(actorDomainService.usernameAlreadyUse(eq("test"))).doReturn(true)

        assertThrows<IllegalArgumentException> {
            service.execute(RegisterLocalActor("test", "password"), Anonymous)
        }
    }

    @Test
    fun ローカルインスタンスが見つからない場合失敗() = runTest {
        whenever(actorDomainService.usernameAlreadyUse(eq("test"))).doReturn(false)

        assertThrows<InternalServerException> {
            service.execute(RegisterLocalActor("test", "password"), Anonymous)
        }
    }


}