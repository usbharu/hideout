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

import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.support.domain.apHost
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class UserDetailsServiceImpl(
    private val actorRepository: ActorRepository,
    private val userDetailRepository: UserDetailRepository,
    private val applicationConfig: ApplicationConfig,
    private val transaction: Transaction,
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails = runBlocking(MDCContext()) {
        if (username == null) {
            throw UsernameNotFoundException("Username not found")
        }
        transaction.transaction {
            val actor = actorRepository.findByNameAndDomain(username, applicationConfig.url.apHost)
                ?: throw UsernameNotFoundException("$username not found")
            val userDetail = userDetailRepository.findByActorId(actor.id.id)
                ?: throw UsernameNotFoundException("${actor.id.id} not found")
            HideoutUserDetails(
                authorities = HashSet(),
                password = userDetail.password.password,
                actor.name.name,
                userDetailsId = userDetail.id.id
            )
        }
    }
}
