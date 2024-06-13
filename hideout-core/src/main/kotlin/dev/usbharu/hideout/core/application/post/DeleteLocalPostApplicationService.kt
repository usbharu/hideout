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

package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import org.springframework.stereotype.Service

@Service
class DeleteLocalPostApplicationService(
    private val postRepository: PostRepository,
    private val userDetailRepository: UserDetailRepository,
    private val actorRepository: ActorRepository,
) {
    suspend fun delete(postId: Long, userDetailId: Long) {
        val findById = postRepository.findById(PostId(postId))!!
        val user = userDetailRepository.findById(userDetailId)!!
        val actor = actorRepository.findById(user.actorId)!!
        findById.delete(actor)
        postRepository.save(findById)
    }
}