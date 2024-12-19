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

package dev.usbharu.hideout.core.application.application

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.application.Application
import dev.usbharu.hideout.core.domain.model.application.ApplicationId
import dev.usbharu.hideout.core.domain.model.application.ApplicationName
import dev.usbharu.hideout.core.domain.model.application.ApplicationRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.service.userdetail.PasswordEncoder
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.SecureTokenGenerator
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Duration

@Service
class RegisterApplicationApplicationService(
    private val idGenerateService: IdGenerateService,
    private val passwordEncoder: PasswordEncoder,
    private val secureTokenGenerator: SecureTokenGenerator,
    private val registeredClientRepository: RegisteredClientRepository,
    transaction: Transaction,
    private val applicationRepository: ApplicationRepository,
) : AbstractApplicationService<RegisterApplication, dev.usbharu.hideout.core.application.model.Application>(
    transaction,
    logger
) {

    override suspend fun internalExecute(
        command: RegisterApplication,
        principal: Principal
    ): dev.usbharu.hideout.core.application.model.Application {
        val id = idGenerateService.generateId()
        val clientSecret = secureTokenGenerator.generate()
        val registeredClient = RegisteredClient
            .withId(id.toString())
            .clientId(id.toString())
            .clientSecret(passwordEncoder.encode(clientSecret))
            .clientName(command.name)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .apply {
                if (command.useRefreshToken) {
                    authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                } else {
                    tokenSettings(
                        TokenSettings
                            .builder()
                            .accessTokenTimeToLive(Duration.ofSeconds(31536000000))
                            .build()
                    )
                }
            }
            .redirectUris { set ->
                set.addAll(command.redirectUris.map { it.toString() })
            }
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .scopes { it.addAll(command.scopes) }
            .build()
        registeredClientRepository.save(registeredClient)

        val application = Application(ApplicationId(id), ApplicationName(command.name))

        applicationRepository.save(application)
        return dev.usbharu.hideout.core.application.model.Application(
            id = id,
            name = command.name,
            clientSecret = clientSecret,
            clientId = id.toString(),
            redirectUris = command.redirectUris
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RegisterApplicationApplicationService::class.java)
    }
}

data class RegisterApplication(
    val name: String,
    val redirectUris: Set<URI>,
    val useRefreshToken: Boolean,
    val scopes: Set<String>,
)
