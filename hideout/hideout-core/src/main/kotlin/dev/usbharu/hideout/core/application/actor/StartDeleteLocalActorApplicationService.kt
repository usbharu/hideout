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
import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StartDeleteLocalActorApplicationService(
    transaction: Transaction,
    private val actorRepository: ActorRepository,
) : LocalUserAbstractApplicationService<DeleteLocalActor, Unit>(transaction, logger) {
    override suspend fun internalExecute(command: DeleteLocalActor, principal: LocalUser) {
        if (command.actorId != principal.actorId) {
            throw PermissionDeniedException()
        }
        val findById = actorRepository.findById(command.actorId)
            ?: throw InternalServerException("Actor ${command.actorId} Not found")
        findById.delete()
        actorRepository.save(findById)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StartDeleteLocalActorApplicationService::class.java)
    }
}

data class DeleteLocalActor(val actorId: ActorId)
