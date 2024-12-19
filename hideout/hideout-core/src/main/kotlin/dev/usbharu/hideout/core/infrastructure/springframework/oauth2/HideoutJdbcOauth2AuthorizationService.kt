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

package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.support.lob.DefaultLobHandler
import org.springframework.jdbc.support.lob.LobHandler
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Component

@Component
class HideoutJdbcOauth2AuthorizationService(
    registeredClientRepository: RegisteredClientRepository,
    jdbcOperations: JdbcOperations,
    @Autowired(required = false) lobHandler: LobHandler = DefaultLobHandler(),
) : JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository, lobHandler) {

    init {
        super.setAuthorizationRowMapper(
            HideoutOAuth2AuthorizationRowMapper(registeredClientRepository = registeredClientRepository)
        )
    }

    class HideoutOAuth2AuthorizationRowMapper(registeredClientRepository: RegisteredClientRepository?) :
        OAuth2AuthorizationRowMapper(registeredClientRepository) {
        init {
            objectMapper.addMixIn(HideoutUserDetails::class.java, UserDetailsMixin::class.java)
        }
    }
}
