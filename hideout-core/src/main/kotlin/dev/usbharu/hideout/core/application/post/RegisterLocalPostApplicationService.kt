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

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostOverview
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.infrastructure.factory.PostFactoryImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RegisterLocalPostApplicationService(
    private val postFactory: PostFactoryImpl,
    private val actorRepository: ActorRepository,
    private val postRepository: PostRepository,
    private val userDetailRepository: UserDetailRepository,
    transaction: Transaction,
) : AbstractApplicationService<RegisterLocalPost, Long>(transaction, Companion.logger) {

    override suspend fun internalExecute(command: RegisterLocalPost, principal: Principal): Long {
        val actorId = (
                userDetailRepository.findById(UserDetailId(command.userDetailId))
                ?: throw IllegalStateException("actor not found")
            ).actorId

        val actor = actorRepository.findById(actorId)!!

        val post = postFactory.createLocal(
            actor = actor,
            actorName = actor.name,
            overview = command.overview?.let { PostOverview(it) },
            content = command.content,
            visibility = command.visibility,
            repostId = command.repostId?.let { PostId(it) },
            replyId = command.replyId?.let { PostId(it) },
            sensitive = command.sensitive,
            mediaIds = command.mediaIds.map { MediaId(it) },
        )

        postRepository.save(post)

        return post.id.id
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RegisterLocalPostApplicationService::class.java)
    }
}
