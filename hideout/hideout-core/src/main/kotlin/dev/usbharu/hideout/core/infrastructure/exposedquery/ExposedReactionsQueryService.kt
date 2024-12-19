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

package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.application.model.Reactions
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.AbstractRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.CustomEmojis
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toCustomEmojiOrNull
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toReaction
import dev.usbharu.hideout.core.query.reactions.ReactionsQueryService
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.net.URI
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Reactions as ExposedrepositoryReactions

@Repository
class ExposedReactionsQueryService : ReactionsQueryService, AbstractRepository(logger) {

    override suspend fun findAllByPostId(postId: PostId): List<Reactions> {
        return query {
            ExposedrepositoryReactions.leftJoin(CustomEmojis).selectAll()
                .where { ExposedrepositoryReactions.postId eq postId.id }
                .groupBy {
                    it[ExposedrepositoryReactions.customEmojiId]?.toString()
                        ?: it[ExposedrepositoryReactions.unicodeEmoji]
                }
                .map { it.value }
                .map {
                    Reactions.of(
                        it.map { resultRow -> resultRow.toReaction() },
                        it.first().toCustomEmojiOrNull()
                    )
                }
        }
    }

    override suspend fun findAllByPostIdIn(postIds: List<PostId>): List<Reactions> {
        return query {
            val actorIdsQuery =
                ExposedrepositoryReactions.actorId.castTo<String>(VarCharColumnType()).groupConcat(",", true)

            ExposedrepositoryReactions.leftJoin(CustomEmojis)
                .select(
                    ExposedrepositoryReactions.postId,
                    ExposedrepositoryReactions.postId.count(),
                    ExposedrepositoryReactions.customEmojiId.max(),
                    ExposedrepositoryReactions.unicodeEmoji.max<String, String>(),
                    actorIdsQuery
                )
                .where { ExposedrepositoryReactions.postId inList postIds.map { it.id } }
                .groupBy(ExposedrepositoryReactions.postId)
                .map {
                    Reactions(
                        postId = it[ExposedrepositoryReactions.postId],
                        count = it[ExposedrepositoryReactions.postId.count()].toInt(),
                        name = it.getOrNull(CustomEmojis.name)
                            ?: it[ExposedrepositoryReactions.unicodeEmoji.max<String, String>()]!!,
                        domain = it.getOrNull(CustomEmojis.domain) ?: UnicodeEmoji.domain.domain,
                        url = it.getOrNull(CustomEmojis.url)?.let { it1 -> URI.create(it1) },
                        actorIds = it[actorIdsQuery].split(",").mapNotNull { s -> s.toLongOrNull() }
                    )
                }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedReactionsQueryService::class.java)
    }
}
