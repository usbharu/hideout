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

package dev.usbharu.hideout.core.infrastructure.exposed

import dev.usbharu.hideout.core.domain.model.actor.*
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.support.domain.Domain
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component

@Component
class ActorResultRowMapper : ResultRowMapper<Actor> {
    override fun map(resultRow: ResultRow): Actor {
        return Actor(
            id = ActorId(resultRow[Actors.id]),
            name = ActorName(resultRow[Actors.name]),
            domain = Domain(resultRow[Actors.domain]),
            screenName = ActorScreenName(resultRow[Actors.screenName]),
            description = ActorDescription(resultRow[Actors.description]),
            inbox = resultRow[Actors.inbox],
            outbox = resultRow[Actors.outbox],
            url = resultRow[Actors.url],
            publicKey = ActorPublicKey(resultRow[Actors.publicKey]),
            privateKey = resultRow[Actors.privateKey]?.let { ActorPrivateKey(it) },
            createdAt = resultRow[Actors.createdAt],
            keyId = ActorKeyId(resultRow[Actors.keyId]),
            followersEndpoint = resultRow[Actors.followers],
            followingEndpoint = resultRow[Actors.following],
            instance = InstanceId(resultRow[Actors.instance]),
            locked = resultRow[Actors.locked],
            followersCount = resultRow[Actors.followersCount]?.let { ActorRelationshipCount(it) },
            followingCount = resultRow[Actors.followingCount]?.let { ActorRelationshipCount(it) },
            postsCount = ActorPostsCount(resultRow[Actors.postsCount]),
            lastPostAt = resultRow[Actors.lastPostAt],
            suspend = resultRow[Actors.suspend],
            lastUpdateAt = resultRow[Actors.lastUpdateAt],
            alsoKnownAs = emptySet(),
            moveTo = resultRow[Actors.moveTo]?.let { ActorId(it) },
            emojiIds = resultRow[Actors.emojis]
                .split(",")
                .filter { it.isNotEmpty() }
                .map { CustomEmojiId(it.toLong()) }
                .toSet(),
            deleted = resultRow[Actors.deleted],
            icon = resultRow[Actors.icon]?.let { MediaId(it) },
            banner = resultRow[Actors.banner]?.let { MediaId(it) }
        )
    }
}
