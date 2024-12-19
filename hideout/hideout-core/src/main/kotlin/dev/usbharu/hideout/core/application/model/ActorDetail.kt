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

package dev.usbharu.hideout.core.application.model

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.support.domain.apHost
import java.net.URI

data class ActorDetail(
    val id: Long,
    val name: String,
    val screenName: String,
    val host: String,
    val instanceId: Long,
    val remoteUrl: URI?,
    val locked: Boolean,
    val description: String,
    val postsCount: Int,
    val iconUrl: URI?,
    val bannerURL: URI?,
    val followingCount: Int?,
    val followersCount: Int?,
) {
    companion object {
        fun of(actor: Actor, iconMedia: Media?, bannerMedia: Media?): ActorDetail {
            return ActorDetail(
                id = actor.id.id,
                name = actor.name.name,
                screenName = actor.screenName.screenName,
                host = actor.url.apHost,
                instanceId = actor.instance.instanceId,
                remoteUrl = actor.url,
                locked = actor.locked,
                description = actor.description.description,
                postsCount = actor.postsCount.postsCount,
                iconUrl = iconMedia?.url,
                bannerURL = bannerMedia?.url,
                followingCount = actor.followingCount?.relationshipCount,
                followersCount = actor.followersCount?.relationshipCount,
            )
        }
    }
}
