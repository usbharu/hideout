package dev.usbharu.hideout.activitypub.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
class ActivityPubSecurityConfig {
    @Bean
    @Order(4)
    fun activityPubSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(POST, "/inbox", permitAll)
                authorize(POST, "/users/{username}/inbox", permitAll)
                authorize(GET, "/outbox", permitAll)
                authorize(GET, "/users/{username}/outbox", permitAll)
            }
        }
        return http.build()
    }
}