package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
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
import org.mockito.kotlin.*
import org.springframework.security.core.userdetails.UsernameNotFoundException
import utils.TestTransaction
import java.net.URI
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UserDetailsServiceImplTest {
    @InjectMocks
    lateinit var service: UserDetailsServiceImpl

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var userDetailRepository: UserDetailRepository

    @Spy
    val applicationConfig = ApplicationConfig(URI.create("http://example.com"))

    @Spy
    val transaction = TestTransaction

    @Test
    fun usernameがnullなら失敗() = runTest {
        assertThrows<UsernameNotFoundException> {
            service.loadUserByUsername(null)
        }
        verify(actorRepository, never()).findByNameAndDomain(any(), any())
    }

    @Test
    fun actorが見つからない場合失敗() = runTest {
        assertThrows<UsernameNotFoundException> {
            service.loadUserByUsername("test")
        }
        verify(actorRepository, times(1)).findByNameAndDomain(eq("test"), eq("example.com"))
    }

    @Test
    fun userDetailが見つからない場合失敗() = runTest {
        whenever(actorRepository.findByNameAndDomain(eq("test"), eq("example.com"))).doReturn(
            TestActorFactory.create(
                id = 1, actorName = "test"
            )
        )
        assertThrows<UsernameNotFoundException> {
            service.loadUserByUsername("test")
        }
        verify(actorRepository, times(1)).findByNameAndDomain(eq("test"), eq("example.com"))
        verify(userDetailRepository, times(1)).findByActorId(eq(1))
    }

    @Test
    fun 全部見つかったら成功() = runTest {
        whenever(
            actorRepository.findByNameAndDomain(
                eq("test"),
                eq("example.com")
            )
        ).doReturn(TestActorFactory.create(id = 1))
        whenever(userDetailRepository.findByActorId(eq(1))).doReturn(
            UserDetail.create(
                UserDetailId(1),
                ActorId(1), UserDetailHashedPassword("")
            )
        )

        val actual = service.loadUserByUsername("test")

        assertEquals(HideoutUserDetails(HashSet(), "", "test-1", 1), actual)
    }
}