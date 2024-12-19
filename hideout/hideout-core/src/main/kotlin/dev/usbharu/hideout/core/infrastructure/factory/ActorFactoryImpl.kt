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

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.*
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.support.domain.Domain
import dev.usbharu.hideout.core.domain.model.support.domain.apHost
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.springframework.stereotype.Component
import java.net.URI
import java.time.Instant

@Component
class ActorFactoryImpl(
    private val idGenerateService: IdGenerateService,
    private val applicationConfig: ApplicationConfig,
) {
    suspend fun createLocal(
        name: String,
        keyPair: Pair<ActorPublicKey, ActorPrivateKey>,
        instanceId: InstanceId,
    ): Actor {
        val actorName = ActorName(name)
        val userUrl = "${applicationConfig.url}/users/${actorName.name}"
        return Actor(
            id = ActorId(idGenerateService.generateId()),
            name = actorName,
            domain = Domain(applicationConfig.url.apHost),
            screenName = ActorScreenName(name),
            description = ActorDescription(""),
            inbox = URI.create("$userUrl/inbox"),
            outbox = URI.create("$userUrl/outbox"),
            url = URI.create(userUrl),
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
            lastPostAt = null,
            suspend = false,
            emojiIds = emptySet(),
            deleted = false,
            banner = null,
            icon = null
        )
    }
}

// todo なんか色々おかしいので直す
