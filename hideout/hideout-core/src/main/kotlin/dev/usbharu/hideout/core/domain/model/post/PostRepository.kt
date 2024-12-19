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

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList

interface PostRepository {
    suspend fun save(post: Post): Post
    suspend fun saveAll(posts: List<Post>): List<Post>
    suspend fun findById(id: PostId): Post?
    suspend fun findAllById(ids: List<PostId>): List<Post>
    suspend fun findByActorId(id: ActorId, page: Page? = null): PaginationList<Post, PostId>
    suspend fun delete(post: Post)

    @Suppress("FunctionMaxLength")
    suspend fun findByActorIdAndVisibilityInList(
        actorId: ActorId,
        visibilityList: List<Visibility>,
        of: Page? = null
    ): PaginationList<Post, PostId>
}
