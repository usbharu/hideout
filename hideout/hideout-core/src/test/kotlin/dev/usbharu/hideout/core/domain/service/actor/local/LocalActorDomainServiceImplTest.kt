package dev.usbharu.hideout.core.domain.service.actor.local

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.net.URI

@ExtendWith(MockitoExtension::class)
class LocalActorDomainServiceImplTest {
    @InjectMocks
    lateinit var service: LocalActorDomainServiceImpl

    @Mock
    lateinit var actorRepository: ActorRepository

    @Spy
    val applicationConfig = ApplicationConfig(URI.create("http://example.com"))

    @Test
    fun findByNameAndDomainがnullならfalse() = runTest {
        val actual = service.usernameAlreadyUse("test")

        assertFalse(actual)
    }

    @Test
    fun findByNameAndDomainがnullならtrue() = runTest {
        whenever(actorRepository.findByNameAndDomain(eq("test"), eq("example.com"))).doReturn(TestActorFactory.create())

        val actual = service.usernameAlreadyUse("test")

        assertTrue(actual)
    }

    @Test
    fun generateKeyPair() = runTest {
        service.generateKeyPair()
    }
}