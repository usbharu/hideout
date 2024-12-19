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

package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.model.UserDetail
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetUserDetailApplicationService(
    private val actorRepository: ActorRepository,
    private val userDetailRepository: UserDetailRepository,
    private val customEmojiRepository: CustomEmojiRepository,
    transaction: Transaction,
) :
    AbstractApplicationService<GetUserDetail, UserDetail>(transaction, Companion.logger) {
    override suspend fun internalExecute(command: GetUserDetail, principal: Principal): UserDetail {
        val userDetail = userDetailRepository.findById(UserDetailId(command.id))
            ?: throw IllegalArgumentException("User ${command.id} does not exist")
        val actor = actorRepository.findById(userDetail.actorId)
            ?: throw InternalServerException("Actor ${userDetail.actorId} not found")

        val emojis = customEmojiRepository.findByIds(actor.emojis.map { it.emojiId })

        return UserDetail.of(actor, userDetail, emojis)
    }

    companion object {
        val logger = LoggerFactory.getLogger(GetUserDetailApplicationService::class.java)
    }
}

data class GetUserDetail(val id: Long)
