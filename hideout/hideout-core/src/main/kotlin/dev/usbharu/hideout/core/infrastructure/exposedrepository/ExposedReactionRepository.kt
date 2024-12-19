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

package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionId
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedReactionRepository(override val domainEventPublisher: DomainEventPublisher) :
    ReactionRepository,
    AbstractRepository(logger),
    DomainEventPublishableRepository<Reaction> {

    override suspend fun save(reaction: Reaction): Reaction {
        return query {
            Reactions.upsert {
                it[Reactions.id] = reaction.id.value
                it[Reactions.postId] = reaction.postId.id
                it[Reactions.actorId] = reaction.actorId.id
                it[Reactions.customEmojiId] = reaction.customEmojiId?.emojiId
                it[Reactions.unicodeEmoji] = reaction.unicodeEmoji.name
                it[Reactions.createdAt] = reaction.createdAt
            }
            onComplete {
                update(reaction)
            }
            reaction
        }
    }

    override suspend fun findById(reactionId: ReactionId): Reaction? {
        return query {
            Reactions.selectAll().where {
                Reactions.id eq reactionId.value
            }.singleOrNull()?.toReaction()
        }
    }

    override suspend fun findByPostId(postId: PostId): List<Reaction> {
        return query {
            Reactions.selectAll().where {
                Reactions.postId eq postId.id
            }.map { it.toReaction() }
        }
    }

    override suspend fun existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
        postId: PostId,
        actorId: ActorId,
        customEmojiId: CustomEmojiId?,
        unicodeEmoji: String
    ): Boolean {
        return query {
            Reactions.selectAll().where {
                Reactions.postId.eq(postId.id).and(Reactions.actorId eq actorId.id)
                    .and(
                        (Reactions.customEmojiId eq customEmojiId?.emojiId or (Reactions.unicodeEmoji eq unicodeEmoji))
                    )
            }.empty().not()
        }
    }

    override suspend fun delete(reaction: Reaction) {
        return query {
            Reactions.deleteWhere {
                Reactions.id eq reaction.id.value
            }
            onComplete {
                update(reaction)
            }
        }
    }

    override suspend fun findByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
        postId: PostId,
        actorId: ActorId,
        customEmojiId: CustomEmojiId?,
        unicodeEmoji: String
    ): Reaction? {
        return query {
            Reactions.selectAll().where {
                Reactions.postId.eq(postId.id).and(Reactions.actorId eq actorId.id)
                    .and(
                        (Reactions.customEmojiId eq customEmojiId?.emojiId or (Reactions.unicodeEmoji eq unicodeEmoji))
                    )
            }.limit(1).singleOrNull()?.toReaction()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedReactionRepository::class.java)
    }
}

fun ResultRow.toReaction(): Reaction {
    return Reaction(
        id = ReactionId(this[Reactions.id]),
        postId = PostId(this[Reactions.postId]),
        actorId = ActorId(this[Reactions.actorId]),
        customEmojiId = this[Reactions.customEmojiId]?.let { CustomEmojiId(it) },
        unicodeEmoji = UnicodeEmoji(this[Reactions.unicodeEmoji]),
        createdAt = this[Reactions.createdAt]
    )
}

object Reactions : Table("reactions") {
    val id = long("id")
    val postId = long("post_id").references(Posts.id)
    val actorId = long("actor_id").references(Actors.id)
    val customEmojiId = long("custom_emoji_id").references(CustomEmojis.id).nullable()
    val unicodeEmoji = varchar("unicode_emoji", 100)
    val createdAt = timestamp("created_at")
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
