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

package dev.usbharu.hideout.core.domain.model.actor

import dev.usbharu.hideout.core.domain.event.actor.ActorDomainEventFactory
import dev.usbharu.hideout.core.domain.event.actor.ActorEvent.*
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.support.domain.Domain
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.net.URI
import java.time.Instant

@Suppress("LongParameterList", "ClassOrdering")
class Actor(
    val id: ActorId,
    val name: ActorName,
    val domain: Domain,
    screenName: ActorScreenName,
    description: ActorDescription,
    val inbox: URI,
    val outbox: URI,
    val url: URI,
    val publicKey: ActorPublicKey,
    val privateKey: ActorPrivateKey? = null,
    val createdAt: Instant,
    val keyId: ActorKeyId,
    val followersEndpoint: URI?,
    val followingEndpoint: URI?,
    val instance: InstanceId,
    var locked: Boolean,
    var followersCount: ActorRelationshipCount?,
    var followingCount: ActorRelationshipCount?,
    var postsCount: ActorPostsCount,
    var lastPostAt: Instant? = null,
    suspend: Boolean,
    var lastUpdateAt: Instant = createdAt,
    alsoKnownAs: Set<ActorId> = emptySet(),
    moveTo: ActorId? = null,
    emojiIds: Set<CustomEmojiId>,
    deleted: Boolean,
    icon: MediaId?,
    banner: MediaId?,
) : DomainEventStorable() {

    var banner = banner
        private set

    fun setBannerUrl(banner: MediaId?) {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(UPDATE))
        this.banner = banner
    }

    var icon = icon
        private set

    fun setIconUrl(icon: MediaId?) {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(UPDATE))
        this.icon = icon
    }

    var suspend = suspend
        set(value) {
            if (field != value && value) {
                addDomainEvent(ActorDomainEventFactory(this).createEvent(ACTOR_SUSPEND))
            } else if (field != value && !value) {
                addDomainEvent(ActorDomainEventFactory(this).createEvent(ACTOR_UNSUSPEND))
            }
            field = value
        }

    var alsoKnownAs = alsoKnownAs
        private set

    fun setAlsoKnownAs(alsoKnownAs: Set<ActorId>) {
        require(alsoKnownAs.none { it == id })
        this.alsoKnownAs = alsoKnownAs
    }

    var moveTo = moveTo
        private set

    fun setMoveTo(moveTo: ActorId?) {
        require(moveTo != id)
        addDomainEvent(ActorDomainEventFactory(this).createEvent(MOVE))
        this.moveTo = moveTo
    }

    var emojis = emojiIds
        private set

    var description = description
        private set

    fun setDescription(description: ActorDescription) {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(UPDATE))
        this.description = description
    }

    var screenName = screenName
        private set

    fun setScreenName(screenName: ActorScreenName) {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(UPDATE))
        this.screenName = screenName
    }

    var deleted = deleted
        private set

    fun delete() {
        if (deleted.not()) {
            addDomainEvent(ActorDomainEventFactory(this).createEvent(DELETE))
            screenName = ActorScreenName.empty
            description = ActorDescription.empty
            emojis = emptySet()
            lastPostAt = null
            postsCount = ActorPostsCount.ZERO
            followersCount = null
            followingCount = null
        }
    }

    fun restore() {
        deleted = false
        checkUpdate()
    }

    fun checkUpdate() {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(CHECK_UPDATE))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Actor

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Actor(" +
            "id=$id, " +
            "name=$name, " +
            "domain=$domain, " +
            "inbox=$inbox, " +
            "outbox=$outbox, " +
            "url=$url, " +
            "publicKey=$publicKey, " +
            "privateKey=$privateKey, " +
            "createdAt=$createdAt, " +
            "keyId=$keyId, " +
            "followersEndpoint=$followersEndpoint, " +
            "followingEndpoint=$followingEndpoint, " +
            "instance=$instance, " +
            "locked=$locked, " +
            "followersCount=$followersCount, " +
            "followingCount=$followingCount, " +
            "postsCount=$postsCount, " +
            "lastPostAt=$lastPostAt, " +
            "lastUpdateAt=$lastUpdateAt, " +
            "banner=$banner, " +
            "icon=$icon, " +
            "suspend=$suspend, " +
            "alsoKnownAs=$alsoKnownAs, " +
            "moveTo=$moveTo, " +
            "emojis=$emojis, " +
            "description=$description, " +
            "screenName=$screenName, " +
            "deleted=$deleted" +
            ")"
    }
}
