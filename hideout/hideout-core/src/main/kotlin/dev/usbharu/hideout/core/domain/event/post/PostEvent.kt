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

package dev.usbharu.hideout.core.domain.event.post

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class PostDomainEventFactory(private val post: Post, private val actor: Actor? = null) {
    fun createEvent(postEvent: PostEvent): DomainEvent<PostEventBody> {
        return DomainEvent.create(
            postEvent.eventName,
            PostEventBody(post.id, actor?.id)
        )
    }
}

class PostEventBody(post: PostId, actor: ActorId?) : DomainEventBody(mapOf("post" to post, "actor" to actor)) {
    fun getPostId(): PostId = toMap()["post"] as PostId
    fun getActorId(): ActorId? = toMap()["actor"] as? ActorId
}

enum class PostEvent(val eventName: String) {
    DELETE("PostDelete"),
    UPDATE("PostUpdate"),
    CREATE("PostCreate"),
    CHECK_UPDATE("PostCheckUpdate"),
}
