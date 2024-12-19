package dev.usbharu.hideout.core.application.application

import dev.usbharu.hideout.core.domain.model.application.ApplicationRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.infrastructure.other.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityPasswordEncoder
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.SecureTokenGenerator
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import utils.TestTransaction
import java.net.URI
import java.time.Duration

@ExtendWith(MockitoExtension::class)
class RegisterApplicationApplicationServiceTest {
    @InjectMocks
    lateinit var service: RegisterApplicationApplicationService

    @Mock
    lateinit var secureTokenGenerator: SecureTokenGenerator

    @Mock
    lateinit var registeredClientRepository: RegisteredClientRepository

    @Mock
    lateinit var applicationRepository: ApplicationRepository

    @Spy
    val idGenerateService = TwitterSnowflakeIdGenerateService

    @Spy
    val passwordEncoder = SpringSecurityPasswordEncoder(BCryptPasswordEncoder())

    @Spy
    val transaction = TestTransaction

    @Test
    fun applicationを作成できる() = runTest {
        whenever(secureTokenGenerator.generate()).doReturn("very-secure-token")

        service.execute(
            RegisterApplication("test", setOf(URI.create("https://example.com")), false, setOf("write")),
            Anonymous
        )

        argumentCaptor<RegisteredClient> {
            verify(registeredClientRepository).save(capture())
            val first = allValues.first()
            assertThat(first.tokenSettings.accessTokenTimeToLive).isGreaterThanOrEqualTo(Duration.ofSeconds(31536000000))

        }
    }

    @Test
    fun refreshTokenを有効化してapplicationを作成できる() = runTest {
        whenever(secureTokenGenerator.generate()).doReturn("very-secure-token")

        service.execute(
            RegisterApplication("test", setOf(URI.create("https://example.com")), true, setOf("write")),
            Anonymous
        )

        argumentCaptor<RegisteredClient> {
            verify(registeredClientRepository).save(capture())
            val first = allValues.first()
            assertThat(first.authorizationGrantTypes).contains(AuthorizationGrantType.REFRESH_TOKEN)

        }
    }
}