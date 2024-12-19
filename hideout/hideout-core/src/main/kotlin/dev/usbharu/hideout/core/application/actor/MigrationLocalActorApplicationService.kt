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
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.service.actor.local.AccountMigrationCheck.*
import dev.usbharu.hideout.core.domain.service.actor.local.LocalActorMigrationCheckDomainService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MigrationLocalActorApplicationService(
    private val actorRepository: ActorRepository,
    private val localActorMigrationCheckDomainService: LocalActorMigrationCheckDomainService,
    transaction: Transaction,
    private val userDetailRepository: UserDetailRepository,
) : LocalUserAbstractApplicationService<MigrationLocalActor, Unit>(transaction, logger) {

    override suspend fun internalExecute(command: MigrationLocalActor, principal: LocalUser) {
        if (command.from != principal.actorId.id) {
            throw PermissionDeniedException()
        }

        val userDetail = userDetailRepository.findById(principal.userDetailId)
            ?: throw InternalServerException("User detail ${principal.userDetailId} not found.")

        val fromActorId = ActorId(command.from)
        val toActorId = ActorId(command.to)

        val fromActor =
            actorRepository.findById(fromActorId) ?: throw IllegalArgumentException("Actor ${command.from} not found.")
        val toActor =
            actorRepository.findById(toActorId) ?: throw IllegalArgumentException("Actor ${command.to} not found.")

        val canAccountMigration =
            localActorMigrationCheckDomainService.canAccountMigration(userDetail, fromActor, toActor)
        if (canAccountMigration.canMigration) {
            fromActor.setMoveTo(toActorId)
            actorRepository.save(fromActor)
        } else {
            when (canAccountMigration) {
                is AlreadyMoved -> throw IllegalArgumentException(canAccountMigration.message)
                is CanAccountMigration -> throw InternalServerException()
                is CircularReferences -> throw IllegalArgumentException(canAccountMigration.message)
                is SelfReferences -> throw IllegalArgumentException("Self references are not supported")
                is AlsoKnownAsNotFound -> throw IllegalArgumentException(canAccountMigration.message)
                is MigrationCoolDown -> throw IllegalArgumentException(canAccountMigration.message)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MigrationLocalActorApplicationService::class.java)
    }
}

data class MigrationLocalActor(val from: Long, val to: Long)
