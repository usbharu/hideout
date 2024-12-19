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
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.post.*
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component
import java.net.URI

@Component
class PostResultRowMapper : ResultRowMapper<Post> {
    override fun map(resultRow: ResultRow): Post {
        return Post(
            id = PostId(resultRow[Posts.id]),
            actorId = ActorId(resultRow[Posts.actorId]),
            instanceId = InstanceId(resultRow[Posts.instanceId]),
            overview = resultRow[Posts.overview]?.let { PostOverview(it) },
            content = PostContent(resultRow[Posts.text], resultRow[Posts.content], emptyList()),
            createdAt = resultRow[Posts.createdAt],
            visibility = Visibility.valueOf(resultRow[Posts.visibility]),
            url = URI.create(resultRow[Posts.url]),
            repostId = resultRow[Posts.repostId]?.let { PostId(it) },
            replyId = resultRow[Posts.replyId]?.let { PostId(it) },
            sensitive = resultRow[Posts.sensitive],
            apId = URI.create(resultRow[Posts.apId]),
            deleted = resultRow[Posts.deleted],
            mediaIds = emptyList(),
            visibleActors = emptySet(),
            hide = resultRow[Posts.hide],
            moveTo = resultRow[Posts.moveTo]?.let { PostId(it) }
        )
    }
}
