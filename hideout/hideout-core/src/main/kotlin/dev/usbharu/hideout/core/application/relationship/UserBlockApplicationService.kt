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

package dev.usbharu.hideout.core.application.relationship

import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.service.relationship.RelationshipDomainService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserBlockApplicationService(
    private val relationshipRepository: RelationshipRepository,
    transaction: Transaction,
    private val actorRepository: ActorRepository,
    private val relationshipDomainService: RelationshipDomainService,
) :
    LocalUserAbstractApplicationService<Block, Unit>(transaction, logger) {
    override suspend fun internalExecute(command: Block, principal: LocalUser) {
        val actor = actorRepository.findById(principal.actorId)
            ?: throw IllegalStateException("Actor ${principal.actorId} not found")

        val targetId = ActorId(command.targetActorId)
        val relationship = relationshipRepository.findByActorIdAndTargetId(actor.id, targetId) ?: Relationship.default(
            actor.id,
            targetId
        )

        val inverseRelationship =
            relationshipRepository.findByActorIdAndTargetId(targetId, actor.id) ?: Relationship.default(
                targetId,
                actor.id
            )

        relationshipDomainService.block(relationship, inverseRelationship)

        relationshipRepository.save(relationship)
        relationshipRepository.save(inverseRelationship)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserBlockApplicationService::class.java)
    }
}

data class Block(val targetActorId: Long)
