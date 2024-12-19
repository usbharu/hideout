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
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorName
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostOverview
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.springframework.stereotype.Component
import java.net.URI
import java.time.Instant

@Component
class PostFactoryImpl(
    private val idGenerateService: IdGenerateService,
    private val postContentFactoryImpl: PostContentFactoryImpl,
    private val applicationConfig: ApplicationConfig,
) {
    @Suppress("LongParameterList")
    suspend fun createLocal(
        actor: Actor,
        actorName: ActorName,
        overview: PostOverview?,
        content: String,
        visibility: Visibility,
        repostId: PostId?,
        replyId: PostId?,
        sensitive: Boolean,
        mediaIds: List<MediaId>,
    ): Post {
        val id = idGenerateService.generateId()
        val url = URI.create(applicationConfig.url.toString() + "/users/" + actorName.name + "/posts/" + id)
        return Post.create(
            id = PostId(id),
            actorId = actor.id,
            instanceId = actor.instance,
            overview = overview,
            content = postContentFactoryImpl.create(content),
            createdAt = Instant.now(),
            visibility = visibility,
            url = url,
            repostId = repostId,
            replyId = replyId,
            sensitive = sensitive,
            apId = url,
            deleted = false,
            mediaIds = mediaIds,
            actor = actor,
        )
    }
}
