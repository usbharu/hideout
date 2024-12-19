/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.HideoutUserDetails
import dev.usbharu.hideout.util.RsaUtil
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

@Configuration
@EnableWebSecurity(debug = false)
class SecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @Order(1)
    fun oauth2Provider(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        http {
            exceptionHandling {
                authenticationEntryPoint = LoginUrlAuthenticationEntryPoint("/auth/sign_in")
            }
        }
        return http.build()
    }

    @Bean
    @Order(3)
    fun httpSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/error", permitAll)
                authorize("/auth/sign_in", permitAll)
                authorize(GET, "/.well-known/**", permitAll)
                authorize(GET, "/nodeinfo/2.0", permitAll)

                authorize(GET, "/auth/sign_up", hasRole("ANONYMOUS"))
                authorize(POST, "/auth/sign_up", permitAll)
                authorize(GET, "/users/{username}/posts/{postId}", permitAll)
                authorize(GET, "/users/{userid}", permitAll)
                authorize(GET, "/files/*", permitAll)
                authorize(POST, "/publish", authenticated)
                authorize(GET, "/publish", authenticated)
                authorize(GET, "/", permitAll)

                authorize(anyRequest, authenticated)
            }
            formLogin {
                loginPage = "/auth/sign_in"
                loginProcessingUrl = "/login"
                defaultSuccessUrl("/home", false)
            }
            logout {
                logoutUrl = "/auth/sign_out"
                logoutSuccessUrl = "/auth/sign_in"
            }
        }
        return http.build()
    }

    @Bean
    fun registeredClientRepository(jdbcOperations: JdbcOperations): RegisteredClientRepository =
        JdbcRegisteredClientRepository(jdbcOperations)

    @Bean
    @Suppress("FunctionMaxLength")
    fun oauth2AuthorizationConsentService(
        jdbcOperations: JdbcOperations,
        registeredClientRepository: RegisteredClientRepository,
    ): OAuth2AuthorizationConsentService =
        JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository)

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder().authorizationEndpoint("/oauth/authorize")
            .tokenEndpoint("/oauth/token").tokenRevocationEndpoint("/oauth/revoke").build()
    }

    @Bean
    fun jwtTokenCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context: JwtEncodingContext ->

            if (OAuth2TokenType.ACCESS_TOKEN == context.tokenType &&
                context.authorization?.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE
            ) {
                val userDetailsImpl = context.getPrincipal<Authentication>().principal as HideoutUserDetails
                context.claims.claim("uid", userDetailsImpl.userDetailsId.toString())
            }
        }
    }

    @Bean
    @Suppress("UnsafeCallOnNullableType")
    fun loadJwkSource(jwkConfig: JwkConfig, applicationConfig: ApplicationConfig): JWKSource<SecurityContext> {
        if (jwkConfig.keyId == null) {
            logger.error("hideout.security.jwt.keyId is null.")
        }
        if (jwkConfig.publicKey == null) {
            logger.error("hideout.security.jwt.publicKey is null.")
        }
        if (jwkConfig.privateKey == null) {
            logger.error("hideout.security.jwt.privateKey is null.")
        }
        if (jwkConfig.keyId == null || jwkConfig.publicKey == null || jwkConfig.privateKey == null) {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(applicationConfig.keySize)
            val generateKeyPair = keyPairGenerator.generateKeyPair()

            jwkConfig.keyId = UUID.randomUUID().toString()
            jwkConfig.publicKey = RsaUtil.encodeRsaPublicKey(generateKeyPair.public as RSAPublicKey)
            jwkConfig.privateKey = RsaUtil.encodeRsaPrivateKey(generateKeyPair.private as RSAPrivateKey)
            logger.error(
                """
                |==============
                |==============
                |
                |**Write the following settings in application.yml**
                |
                |hideout:
                |   security:
                |       jwt:
                |           keyId: ${jwkConfig.keyId}
                |           publicKey: ${jwkConfig.publicKey}
                |           privateKey: ${jwkConfig.privateKey}
                |
                |==============
                |==============
                """.trimMargin()
            )
        }

        val rsaKey = RSAKey.Builder(RsaUtil.decodeRsaPublicKey(jwkConfig.publicKey!!))
            .privateKey(RsaUtil.decodeRsaPrivateKey(jwkConfig.privateKey!!)).keyID(jwkConfig.keyId).build()
        return ImmutableJWKSet(JWKSet(rsaKey))
    }

    @ConfigurationProperties("hideout.security.jwt")
    data class JwkConfig(
        var keyId: String?,
        var publicKey: String?,
        var privateKey: String?,
    )

    @Bean
    fun roleHierarchy(): RoleHierarchy {
        val roleHierarchyImpl = RoleHierarchyImpl.fromHierarchy(
            """
            SCOPE_read > SCOPE_read:accounts
            SCOPE_read > SCOPE_read:accounts
            SCOPE_read > SCOPE_read:blocks
            SCOPE_read > SCOPE_read:bookmarks
            SCOPE_read > SCOPE_read:favourites
            SCOPE_read > SCOPE_read:filters
            SCOPE_read > SCOPE_read:follows
            SCOPE_read > SCOPE_read:lists
            SCOPE_read > SCOPE_read:mutes
            SCOPE_read > SCOPE_read:notifications
            SCOPE_read > SCOPE_read:search
            SCOPE_read > SCOPE_read:statuses
            SCOPE_write > SCOPE_write:accounts
            SCOPE_write > SCOPE_write:blocks
            SCOPE_write > SCOPE_write:bookmarks
            SCOPE_write > SCOPE_write:conversations
            SCOPE_write > SCOPE_write:favourites
            SCOPE_write > SCOPE_write:filters
            SCOPE_write > SCOPE_write:follows
            SCOPE_write > SCOPE_write:lists
            SCOPE_write > SCOPE_write:media
            SCOPE_write > SCOPE_write:mutes
            SCOPE_write > SCOPE_write:notifications
            SCOPE_write > SCOPE_write:reports
            SCOPE_write > SCOPE_write:statuses
            SCOPE_follow > SCOPE_write:blocks
            SCOPE_follow > SCOPE_write:follows
            SCOPE_follow > SCOPE_write:mutes
            SCOPE_follow > SCOPE_read:blocks
            SCOPE_follow > SCOPE_read:follows
            SCOPE_follow > SCOPE_read:mutes
            SCOPE_admin > SCOPE_admin:read
            SCOPE_admin > SCOPE_admin:write
            SCOPE_admin:read > SCOPE_admin:read:accounts
            SCOPE_admin:read > SCOPE_admin:read:reports
            SCOPE_admin:read > SCOPE_admin:read:domain_allows
            SCOPE_admin:read > SCOPE_admin:read:domain_blocks
            SCOPE_admin:read > SCOPE_admin:read:ip_blocks
            SCOPE_admin:read > SCOPE_admin:read:email_domain_blocks
            SCOPE_admin:read > SCOPE_admin:read:canonical_email_blocks
            SCOPE_admin:write > SCOPE_admin:write:accounts
            SCOPE_admin:write > SCOPE_admin:write:reports
            SCOPE_admin:write > SCOPE_admin:write:domain_allows
            SCOPE_admin:write > SCOPE_admin:write:domain_blocks
            SCOPE_admin:write > SCOPE_admin:write:ip_blocks
            SCOPE_admin:write > SCOPE_admin:write:email_domain_blocks
            SCOPE_admin:write > SCOPE_admin:write:canonical_email_blocks
            """.trimIndent()
        )

        return roleHierarchyImpl
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)
    }
}
