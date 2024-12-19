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

import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UnsuspendLocalActorApplicationService(
    transaction: Transaction,
    private val actorRepository: ActorRepository,
) : LocalUserAbstractApplicationService<UnsuspendLocalActor, Unit>(transaction, logger) {

    override suspend fun internalExecute(command: UnsuspendLocalActor, principal: LocalUser) {
        val findById = actorRepository.findById(ActorId(command.actorId))
            ?: throw IllegalArgumentException("Actor ${command.actorId} not found.")

        findById.suspend = false
        actorRepository.save(findById)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UnsuspendLocalActorApplicationService::class.java)
    }
}

data class UnsuspendLocalActor(val actorId: Long)
