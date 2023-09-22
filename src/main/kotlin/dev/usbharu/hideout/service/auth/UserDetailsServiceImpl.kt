package dev.usbharu.hideout.service.auth

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.UserDetailsImpl
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.net.URL

@Service
class UserDetailsServiceImpl(
    private val userQueryService: UserQueryService,
    private val applicationConfig: ApplicationConfig,
    private val transaction: Transaction
) :
    UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails = runBlocking {
        if (username == null) {
            throw UsernameNotFoundException("$username not found")
        }
        transaction.transaction {
            val findById = userQueryService.findByNameAndDomain(username, URL(applicationConfig.url).host)
            UserDetailsImpl(
                findById.id,
                findById.name,
                findById.password,
                true,
                true,
                true,
                true,
                mutableListOf()
            )
        }
    }
}
