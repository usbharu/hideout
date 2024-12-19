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

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsEmojis
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsMedia
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsVisibleActors
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component

@Component
class PostQueryMapper(private val postResultRowMapper: ResultRowMapper<Post>) : QueryMapper<Post> {
    override fun map(query: Query): List<Post> {
        return query
            .groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                it
                    .first()
                    .let(postResultRowMapper::map)
                    .run {
                        buildPost(this, it)
                    }
            }
    }

    private fun buildPost(post: Post, it: List<ResultRow>): Post {
        return post.reconstructWith(
            mediaIds = it.mapNotNull { resultRow: ResultRow ->
                resultRow
                    .getOrNull(PostsMedia.mediaId)
                    ?.let { mediaId -> MediaId(mediaId) }
            },
            emojis = it
                .mapNotNull { resultRow: ResultRow ->
                    resultRow
                        .getOrNull(PostsEmojis.emojiId)
                        ?.let { emojiId -> CustomEmojiId(emojiId) }
                },
            visibleActors = it.mapNotNull { resultRow: ResultRow ->
                resultRow
                    .getOrNull(PostsVisibleActors.actorId)
                    ?.let { actorId -> ActorId(actorId) }
            }.toSet()
        )
    }
}
