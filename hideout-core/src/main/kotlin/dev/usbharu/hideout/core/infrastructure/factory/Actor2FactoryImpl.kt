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

package dev.usbharu.hideout.core.infrastructure.factory

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.actor.*
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.shared.Domain
import org.springframework.stereotype.Component
import java.net.URI
import java.time.Instant

@Component
class Actor2FactoryImpl(
    private val idGenerateService: IdGenerateService,
    private val actorScreenNameFactory: ActorScreenNameFactoryImpl,
    private val actorDescriptionFactory: ActorDescriptionFactoryImpl,
    private val applicationConfig: ApplicationConfig,
) : Actor2.Actor2Factory() {
    suspend fun createLocal(
        name: String,
        keyPair: Pair<ActorPublicKey, ActorPrivateKey>,
        instanceId: InstanceId,
    ): Actor2 {
        val actorName = ActorName(name)
        val userUrl = "${applicationConfig.url}/users/${actorName.name}"
        return super.create(
            id = ActorId(idGenerateService.generateId()),
            name = actorName,
            domain = Domain(applicationConfig.url.host),
            screenName = actorScreenNameFactory.create(name),
            description = actorDescriptionFactory.create(""),
            inbox = URI.create("$userUrl/inbox"),
            outbox = URI.create("$userUrl/outbox"),
            url = applicationConfig.url.toURI(),
            publicKey = keyPair.first,
            privateKey = keyPair.second,
            createdAt = Instant.now(),
            keyId = ActorKeyId("$userUrl#main-key"),
            followersEndpoint = URI.create("$userUrl/followers"),
            followingEndpoint = URI.create("$userUrl/following"),
            instance = instanceId,
            locked = false,
            followersCount = ActorRelationshipCount(0),
            followingCount = ActorRelationshipCount(0),
            postsCount = ActorPostsCount(0),
            lastPostDate = null,
            suspend = false
        )
    }
}