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

import dev.usbharu.hideout.core.application.model.ActorDetail
import dev.usbharu.hideout.core.application.model.MediaDetail
import dev.usbharu.hideout.core.application.model.PostDetail
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import dev.usbharu.hideout.core.query.usertimeline.UserTimelineQueryService
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.net.URI

@Repository
class ExposedUserTimelineQueryService : UserTimelineQueryService, AbstractRepository(logger) {
    protected fun authorizedQuery(principal: Principal? = null): QueryAlias {
        if (principal == null) {
            return Posts
                .selectAll()
                .where {
                    Posts.visibility eq Visibility.PUBLIC.name or (Posts.visibility eq Visibility.UNLISTED.name)
                }.alias("authorized_table")
        }

        val relationshipsAlias = Relationships.alias("inverse_relationships")

        return Posts
            .leftJoin(PostsVisibleActors)
            .leftJoin(Relationships, onColumn = { Posts.actorId }, otherColumn = { actorId })
            .leftJoin(
                relationshipsAlias,
                onColumn = { Posts.actorId },
                otherColumn = { relationshipsAlias[Relationships.targetActorId] }
            )
            .select(Posts.columns)
            .where {
                Posts.visibility eq Visibility.PUBLIC.name or
                        (Posts.visibility eq Visibility.UNLISTED.name) or
                        (Posts.visibility eq Visibility.DIRECT.name and (PostsVisibleActors.actorId eq principal.actorId.id)) or
                        (Posts.visibility eq Visibility.FOLLOWERS.name and (Relationships.blocking eq false and (relationshipsAlias[Relationships.following] eq true))) or
                        (Posts.actorId eq principal.actorId.id)
            }
            .alias("authorized_table")
    }

    override suspend fun findByIdAll(idList: List<PostId>, principal: Principal): List<PostDetail> {
        val authorizedQuery = authorizedQuery(principal)

        val iconMedia = Media.alias("ICON_MEDIA")

        return authorizedQuery
            .leftJoin(PostsVisibleActors, { authorizedQuery[Posts.id] }, { postId })
            .leftJoin(Actors, { authorizedQuery[Posts.actorId] }, { id })
            .leftJoin(iconMedia, { Actors.icon }, { iconMedia[Media.id] })
            .leftJoin(PostsMedia, { authorizedQuery[Posts.id] }, { postId })
            .leftJoin(Media, { PostsMedia.mediaId }, { id })
            .leftJoin(
                Reactions,
                { authorizedQuery[Posts.id] },
                { postId },
                { Reactions.id isDistinctFrom principal.actorId.id }
            )
            .selectAll()
            .where { authorizedQuery[Posts.id] inList idList.map { it.id } }
            .groupBy { it[authorizedQuery[Posts.id]] }
            .map { it.value }
            .map {
                toPostDetail(it.first(), authorizedQuery, iconMedia).copy(
                    mediaDetailList = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.let { it1 -> MediaDetail.of(it1) }
                    },
                    favourited = it.any { resultRow -> resultRow.getOrNull(Reactions.actorId) != null }
                )
            }
    }

    private fun toPostDetail(it: ResultRow, authorizedQuery: QueryAlias, iconMedia: Alias<Media>): PostDetail {
        return PostDetail(
            id = it[authorizedQuery[Posts.id]],
            actor = ActorDetail(
                id = it[authorizedQuery[Posts.actorId]],
                instanceId = it[Actors.instance],
                name = it[Actors.name],
                host = it[Actors.domain],
                screenName = it[Actors.screenName],
                remoteUrl = it[Actors.url],
                locked = it[Actors.locked],
                iconUrl = it.getOrNull(iconMedia[Media.url]),
                description = it[Actors.description],
                postsCount = it[Actors.postsCount],
                bannerURL = null,
                followingCount = it[Actors.followingCount],
                followersCount = it[Actors.followersCount],
            ),
            overview = it[authorizedQuery[Posts.overview]],
            text = it[authorizedQuery[Posts.text]],
            content = it[authorizedQuery[Posts.content]],
            createdAt = it[authorizedQuery[Posts.createdAt]],
            visibility = Visibility.valueOf(it[authorizedQuery[Posts.visibility]]),
            pureRepost = false,
            url = URI.create(it[authorizedQuery[Posts.url]]),
            apId = URI.create(it[authorizedQuery[Posts.apId]]),
            repost = null,
            reply = null,
            sensitive = it[authorizedQuery[Posts.sensitive]],
            deleted = it[authorizedQuery[Posts.deleted]],
            mediaDetailList = emptyList(),
            moveTo = null,
            reactionsList = emptyList(),
            favourited = false
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedUserTimelineQueryService::class.java)
    }
}
