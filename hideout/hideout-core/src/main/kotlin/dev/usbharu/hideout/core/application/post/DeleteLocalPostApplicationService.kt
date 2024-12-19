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

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteLocalPostApplicationService(
    private val postRepository: PostRepository,
    private val actorRepository: ActorRepository,
    transaction: Transaction,
) : LocalUserAbstractApplicationService<DeleteLocalPost, Unit>(transaction, logger) {

    override suspend fun internalExecute(command: DeleteLocalPost, principal: LocalUser) {
        val findById = postRepository.findById(PostId(command.postId))
            ?: throw IllegalArgumentException("Post ${command.postId} not found.")
        if (findById.actorId != principal.actorId) {
            throw PermissionDeniedException()
        }
        val actor = actorRepository.findById(principal.actorId)
            ?: throw InternalServerException("Actor ${principal.actorId} not found.")
        findById.delete(actor)
        postRepository.save(findById)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DeleteLocalPostApplicationService::class.java)
    }
}

data class DeleteLocalPost(val postId: Long)
