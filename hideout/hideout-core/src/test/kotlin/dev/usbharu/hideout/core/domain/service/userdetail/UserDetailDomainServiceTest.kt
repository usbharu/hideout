package dev.usbharu.hideout.core.domain.service.userdetail

import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityPasswordEncoder
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertNotEquals

@ExtendWith(MockitoExtension::class)
class UserDetailDomainServiceTest {

    @InjectMocks
    lateinit var userDetailDomainService: UserDetailDomainService

    @Spy
    val passwordEncoder: PasswordEncoder = SpringSecurityPasswordEncoder(BCryptPasswordEncoder())

    @Test
    fun hash() = runTest {
        val hashedPassword = userDetailDomainService.hashPassword("password")

        assertNotEquals("password", hashedPassword.password)
    }
}