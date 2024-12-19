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

package dev.usbharu.hideout.core.domain.service.actor.local

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorPrivateKey
import dev.usbharu.hideout.core.domain.model.actor.ActorPublicKey
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.support.domain.apHost
import org.springframework.stereotype.Service
import java.security.KeyPairGenerator

@Service
class LocalActorDomainServiceImpl(
    private val actorRepository: ActorRepository,
    private val applicationConfig: ApplicationConfig,
) : LocalActorDomainService {
    override suspend fun usernameAlreadyUse(name: String): Boolean =
        actorRepository.findByNameAndDomain(name, applicationConfig.url.apHost) != null

    override suspend fun generateKeyPair(): Pair<ActorPublicKey, ActorPrivateKey> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(applicationConfig.keySize)
        val generateKeyPair = keyPairGenerator.generateKeyPair()

        return ActorPublicKey.create(generateKeyPair.public) to ActorPrivateKey.create(generateKeyPair.private)
    }
}
