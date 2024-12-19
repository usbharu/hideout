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

package dev.usbharu.hideout.mastodon.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.media.FileType
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Account
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.MediaAttachment
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status.Visibility.*
import dev.usbharu.hideout.mastodon.query.StatusQuery
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.CustomEmoji as MastodonEmoji

@Suppress("IncompleteDestructuring")
@Repository
class StatusQueryServiceImpl : StatusQueryService {

    protected fun authorizedQuery(principal: Principal? = null): QueryAlias {
        if (principal == null) {
            return Posts
                .selectAll()
                .where {
                    Posts.visibility eq Visibility.PUBLIC.name or (Posts.visibility eq Visibility.UNLISTED.name)
                }
                .alias("authorized_table")
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

    override suspend fun findByPostIds(ids: List<Long>): List<Status> = findByPostIdsWithMedia(ids)

    override suspend fun findByPostIdsWithMediaIds(statusQueries: List<StatusQuery>): List<Status> {
        val postIdSet = mutableSetOf<Long>()
        postIdSet.addAll(statusQueries.flatMap { listOfNotNull(it.postId, it.replyId, it.repostId) })
        val mediaIdSet = mutableSetOf<Long>()
        mediaIdSet.addAll(statusQueries.flatMap { it.mediaIds })

        val emojiIdSet = mutableSetOf<Long>()
        emojiIdSet.addAll(statusQueries.flatMap { it.emojiIds })

        val qa = authorizedQuery()
        val replyToAlias = Posts.alias("reply_to")
        val postMap = qa
            .leftJoin(Actors)
            .selectAll().where { Posts.id inList postIdSet }
            .associate { it[Posts.id] to toStatus(it, qa, replyToAlias) }
        val mediaMap = Media.selectAll().where { Media.id inList mediaIdSet }
            .associate {
                it[Media.id] to it.toMedia().toMediaAttachments()
            }

        val emojiMap = CustomEmojis.selectAll().where { CustomEmojis.id inList emojiIdSet }.associate {
            it[CustomEmojis.id] to it.toCustomEmoji().toMastodonEmoji()
        }
        return statusQueries.mapNotNull { statusQuery ->
            postMap[statusQuery.postId]?.copy(
                inReplyToId = statusQuery.replyId?.toString(),
                inReplyToAccountId = postMap[statusQuery.replyId]?.account?.id,
                reblog = postMap[statusQuery.repostId],
                mediaAttachments = statusQuery.mediaIds.mapNotNull { mediaMap[it] },
                emojis = statusQuery.emojiIds.mapNotNull { emojiMap[it] }
            )
        }
    }

    override suspend fun accountsStatus(
        accountId: Long,
        onlyMedia: Boolean,
        excludeReplies: Boolean,
        excludeReblogs: Boolean,
        pinned: Boolean,
        tagged: String?,
        includeFollowers: Boolean,
    ): List<Status> {
        val inReplyToAlias = Posts.alias("reply_to")
        val qa = authorizedQuery()
        val query = qa
            .leftJoin(PostsMedia)
            .leftJoin(Actors)
            .leftJoin(Media)
            .selectAll().where { Posts.actorId eq accountId }

        if (onlyMedia) {
            query.andWhere { PostsMedia.mediaId.isNotNull() }
        }
        if (excludeReplies) {
            query.andWhere { Posts.replyId.isNotNull() }
        }
        if (excludeReblogs) {
            query.andWhere { Posts.repostId.isNotNull() }
        }
        if (includeFollowers) {
            query.andWhere { Posts.visibility inList listOf(public.name, unlisted.name, private.name) }
        } else {
            query.andWhere { Posts.visibility inList listOf(public.name, unlisted.name) }
        }

        val pairs = query
            .groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                toStatus(it.first(), qa, inReplyToAlias).copy(
                    mediaAttachments = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.toMediaAttachments()
                    }
                ) to it.first()[Posts.repostId]
            }

        val statuses = resolveReplyAndRepost(pairs)
        return statuses
    }

    override suspend fun findByPostId(id: Long, principal: Principal?): Status? {
        val aq = authorizedQuery(principal)
        val inReplyTo = Posts.alias("reply_to")
        val map = aq
            .leftJoin(PostsMedia, { aq[Posts.id] }, { PostsMedia.postId })
            .leftJoin(Actors, { aq[Posts.actorId] }, { Actors.id })
            .leftJoin(Media, { PostsMedia.mediaId }, { Media.id })
            .leftJoin(inReplyTo, { aq[Posts.replyId] }, { inReplyTo[Posts.id] })
            .selectAll()
            .where { aq[Posts.id] eq id }
            .groupBy { it[aq[Posts.id]] }
            .map { it.value }
            .map {
                toStatus(it.first(), aq, inReplyTo).copy(
                    mediaAttachments = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.toMediaAttachments()
                    },
                    emojis = it.mapNotNull { resultRow -> resultRow.toCustomEmojiOrNull()?.toMastodonEmoji() }
                ) to it.first()[aq[Posts.repostId]]
            }
        return resolveReplyAndRepost(map).singleOrNull()
    }

    private fun resolveReplyAndRepost(pairs: List<Pair<Status, Long?>>): List<Status> {
        val statuses = pairs.map { it.first }
        return pairs
            .map {
                if (it.second != null) {
                    it.first.copy(reblog = statuses.find { (id) -> id == it.second.toString() })
                } else {
                    it.first
                }
            }
    }

    private suspend fun findByPostIdsWithMedia(ids: List<Long>): List<Status> {
        val inReplyToAlias = Posts.alias("reply_to")
        val qa = authorizedQuery()
        val pairs = Posts
            .leftJoin(PostsMedia)
            .leftJoin(PostsEmojis)
            .leftJoin(CustomEmojis)
            .leftJoin(Actors)
            .leftJoin(Media)
            .selectAll().where { Posts.id inList ids }
            .groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                toStatus(it.first(), qa, inReplyToAlias).copy(
                    mediaAttachments = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.toMediaAttachments()
                    },
                    emojis = it.mapNotNull { resultRow -> resultRow.toCustomEmojiOrNull()?.toMastodonEmoji() }
                ) to it.first()[Posts.repostId]
            }
        return resolveReplyAndRepost(pairs)
    }
}

private fun CustomEmoji.toMastodonEmoji(): MastodonEmoji = MastodonEmoji(
    shortcode = this.name,
    url = this.url.toString(),
    staticUrl = this.url.toString(),
    visibleInPicker = true,
    category = this.category.orEmpty()
)

private fun toStatus(it: ResultRow, queryAlias: QueryAlias, inReplyToAlias: Alias<Posts>) = Status(
    id = it[queryAlias[Posts.id]].toString(),
    uri = it[queryAlias[Posts.apId]],
    createdAt = it[queryAlias[Posts.createdAt]].toString(),
    account = Account(
        id = it[Actors.id].toString(),
        username = it[Actors.name],
        acct = "${it[Actors.name]}@${it[Actors.domain]}",
        url = it[Actors.url].toString(),
        displayName = it[Actors.screenName],
        note = it[Actors.description],
        avatar = "${it[Actors.url]}/icon.jpg",
        avatarStatic = "${it[Actors.url]}/icon.jpg",
        header = "${it[Actors.url]}/header.jpg",
        headerStatic = "${it[Actors.url]}/header.jpg",
        locked = it[Actors.locked],
        fields = emptyList(),
        emojis = emptyList(),
        bot = false,
        group = false,
        discoverable = true,
        createdAt = it[Actors.createdAt].toString(),
        lastStatusAt = it[Actors.lastPostAt]?.toString(),
        statusesCount = it[Actors.postsCount],
        followersCount = it[Actors.followersCount],
        followingCount = it[Actors.followingCount],
        noindex = false,
        moved = false,
        suspended = false,
        limited = false
    ),
    content = it[queryAlias[Posts.text]],
    visibility = when (Visibility.valueOf(it[queryAlias[Posts.visibility]])) {
        Visibility.PUBLIC -> public
        Visibility.UNLISTED -> unlisted
        Visibility.FOLLOWERS -> private
        Visibility.DIRECT -> direct
    },
    sensitive = it[queryAlias[Posts.sensitive]],
    spoilerText = it[queryAlias[Posts.overview]].orEmpty(),
    mediaAttachments = emptyList(),
    mentions = emptyList(),
    tags = emptyList(),
    emojis = emptyList(),
    reblogsCount = 0,
    favouritesCount = 0,
    repliesCount = 0,
    url = it[queryAlias[Posts.apId]],
    inReplyToId = it[queryAlias[Posts.replyId]]?.toString(),
    inReplyToAccountId = it.getOrNull(inReplyToAlias[Posts.actorId])?.toString(),
    language = null,
    text = it[queryAlias[Posts.text]],
    editedAt = null
)

fun EntityMedia.toMediaAttachments(): MediaAttachment = MediaAttachment(
    id = id.id.toString(),
    type = when (type) {
        FileType.Image -> MediaAttachment.Type.image
        FileType.Video -> MediaAttachment.Type.video
        FileType.Audio -> MediaAttachment.Type.audio
        FileType.Unknown -> MediaAttachment.Type.unknown
    },
    url = url.toString(),
    previewUrl = thumbnailUrl?.toString(),
    remoteUrl = remoteUrl?.toString(),
    description = description?.description,
    blurhash = blurHash?.hash,
    textUrl = url.toString()
)
