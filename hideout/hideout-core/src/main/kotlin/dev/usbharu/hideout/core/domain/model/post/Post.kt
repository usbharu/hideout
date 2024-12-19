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

package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.core.domain.event.post.PostDomainEventFactory
import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.net.URI
import java.time.Instant

@Suppress("LongParameterList", "TooManyFunctions", "ClassOrdering")
class Post(
    val id: PostId,
    actorId: ActorId,
    val instanceId: InstanceId,
    overview: PostOverview?,
    content: PostContent,
    val createdAt: Instant,
    visibility: Visibility,
    val url: URI,
    val repostId: PostId?,
    val replyId: PostId?,
    sensitive: Boolean,
    val apId: URI,
    deleted: Boolean,
    mediaIds: List<MediaId>,
    visibleActors: Set<ActorId>,
    hide: Boolean,
    moveTo: PostId?,
) : DomainEventStorable() {

    val actorId = actorId
        get() {
            if (deleted) {
                return ActorId.ghost
            }
            return field
        }

    var visibility = visibility
        private set

    fun setVisibility(visibility: Visibility, actor: Actor) {
        require(this.visibility != Visibility.DIRECT)
        require(visibility != Visibility.DIRECT)
        require(this.visibility.ordinal >= visibility.ordinal)

        require(deleted.not())

        if (this.visibility != visibility) {
            addDomainEvent(PostDomainEventFactory(this, actor).createEvent(PostEvent.UPDATE))
        }
        this.visibility = visibility
    }

    var visibleActors = visibleActors
        private set

    fun setVisibleActors(visibleActors: Set<ActorId>, actor: Actor) {
        require(deleted.not())
        if (visibility == Visibility.DIRECT) {
            addDomainEvent(PostDomainEventFactory(this, actor).createEvent(PostEvent.UPDATE))
            this.visibleActors = this.visibleActors.plus(visibleActors)
        }
    }

    val isPureRepost = content == PostContent.empty && overview == null && replyId == null && repostId != null

    var content = content
        get() {
            if (hide) {
                return PostContent.empty
            }
            return field
        }
        private set

    fun setContent(content: PostContent, actor: Actor) {
        require(deleted.not())
        if (this.content != content) {
            addDomainEvent(PostDomainEventFactory(this, actor).createEvent(PostEvent.UPDATE))
        }
        this.content = content
    }

    var overview = overview
        get() {
            if (hide) {
                return null
            }
            return field
        }
        private set

    fun setOverview(overview: PostOverview?, actor: Actor) {
        require(deleted.not())
        if (this.overview != overview) {
            addDomainEvent(PostDomainEventFactory(this, actor).createEvent(PostEvent.UPDATE))
        }
        this.overview = overview
    }

    var sensitive = sensitive
        private set

    fun setSensitive(sensitive: Boolean, actor: Actor) {
        require(deleted.not())
        if (this.sensitive != sensitive) {
            addDomainEvent(PostDomainEventFactory(this, actor).createEvent(PostEvent.UPDATE))
        }
        this.sensitive = sensitive
    }

    val text: String
        get() {
            if (hide) {
                return PostContent.empty.text
            }
            return content.text
        }

    val emojiIds: List<CustomEmojiId>
        get() {
            if (hide) {
                return PostContent.empty.emojiIds
            }
            return content.emojiIds
        }

    var mediaIds = mediaIds
        get() {
            if (hide) {
                return emptyList()
            }
            return field
        }
        private set

    fun addMediaIds(mediaIds: List<MediaId>, actor: Actor) {
        require(deleted.not())
        addDomainEvent(PostDomainEventFactory(this, actor).createEvent(PostEvent.UPDATE))
        this.mediaIds = this.mediaIds.plus(mediaIds).distinct()
    }

    var deleted = deleted
        private set

    fun delete(actor: Actor) {
        if (deleted.not()) {
            addDomainEvent(PostDomainEventFactory(this, actor).createEvent(PostEvent.DELETE))
            content = PostContent.empty
            overview = null
            mediaIds = emptyList()
        }
        deleted = true
    }

    fun checkUpdate() {
        addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.CHECK_UPDATE))
    }

    fun restore(content: PostContent, overview: PostOverview?, mediaIds: List<MediaId>) {
        require(deleted)
        deleted = false
        this.content = content
        this.overview = overview
        this.mediaIds = mediaIds
        checkUpdate()
    }

    var hide = hide
        private set

    fun hide() {
        hide = true
    }

    fun show() {
        hide = false
    }

    var moveTo = moveTo
        private set

    fun moveTo(moveTo: PostId) {
        require(this.moveTo == null)
        this.moveTo = moveTo
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    fun reconstructWith(mediaIds: List<MediaId>, emojis: List<CustomEmojiId>, visibleActors: Set<ActorId>): Post {
        return Post(
            id = id,
            actorId = actorId,
            instanceId = instanceId,
            overview = overview,
            content = PostContent(this.content.text, this.content.content, emojis),
            createdAt = createdAt,
            visibility = visibility,
            url = url,
            repostId = repostId,
            replyId = replyId,
            sensitive = sensitive,
            apId = apId,
            deleted = deleted,
            mediaIds = mediaIds,
            visibleActors = visibleActors,
            hide = hide,
            moveTo = moveTo
        )
    }

    override fun toString(): String {
        return "Post(" +
            "id=$id, " +
            "createdAt=$createdAt, " +
            "url=$url, " +
            "repostId=$repostId, " +
            "replyId=$replyId, " +
            "apId=$apId, " +
            "actorId=$actorId, " +
            "visibility=$visibility, " +
            "visibleActors=$visibleActors, " +
            "content=$content, " +
            "overview=$overview, " +
            "sensitive=$sensitive, " +
            "text='$text', " +
            "emojiIds=$emojiIds, " +
            "mediaIds=$mediaIds, " +
            "deleted=$deleted, " +
            "hide=$hide, " +
            "moveTo=$moveTo" +
            ")"
    }

    companion object {
        @Suppress("LongParameterList")
        fun create(
            id: PostId,
            actorId: ActorId,
            instanceId: InstanceId,
            overview: PostOverview? = null,
            content: PostContent,
            createdAt: Instant,
            visibility: Visibility,
            url: URI,
            repostId: PostId?,
            replyId: PostId?,
            sensitive: Boolean,
            apId: URI,
            deleted: Boolean,
            mediaIds: List<MediaId>,
            visibleActors: Set<ActorId> = emptySet(),
            hide: Boolean = false,
            moveTo: PostId? = null,
            actor: Actor,
        ): Post {
            require(actor.deleted.not())
            require(actor.moveTo == null)

            val visibility1 = if (actor.suspend && visibility == Visibility.PUBLIC) {
                Visibility.UNLISTED
            } else {
                visibility
            }

            val post = Post(
                id = id,
                actorId = actorId,
                instanceId = instanceId,
                overview = overview,
                content = content,
                createdAt = createdAt,
                visibility = visibility1,
                url = url,
                repostId = repostId,
                replyId = replyId,
                sensitive = sensitive,
                apId = apId,
                deleted = deleted,
                mediaIds = mediaIds,
                visibleActors = visibleActors,
                hide = hide,
                moveTo = moveTo
            )
            post.addDomainEvent(PostDomainEventFactory(post).createEvent(PostEvent.CREATE))
            return post
        }
    }
}
