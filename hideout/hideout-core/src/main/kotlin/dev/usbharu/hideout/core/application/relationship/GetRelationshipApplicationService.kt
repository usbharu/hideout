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

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.model.Relationship
import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actorinstancerelationship.ActorInstanceRelationship
import dev.usbharu.hideout.core.domain.model.actorinstancerelationship.ActorInstanceRelationshipRepository
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetRelationshipApplicationService(
    private val relationshipRepository: RelationshipRepository,
    private val actorRepository: ActorRepository,
    private val actorInstanceRelationshipRepository: ActorInstanceRelationshipRepository,
    transaction: Transaction,
) :
    LocalUserAbstractApplicationService<GetRelationship, Relationship>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: GetRelationship, principal: LocalUser): Relationship {
        val actor = actorRepository.findById(principal.actorId)
            ?: throw InternalServerException("Actor ${principal.actorId} not found.")
        val targetId = ActorId(command.targetActorId)
        val target = actorRepository.findById(targetId)
            ?: throw IllegalArgumentException("Actor ${command.targetActorId} not found.")
        val relationship = (
            relationshipRepository.findByActorIdAndTargetId(actor.id, targetId)
                ?: dev.usbharu.hideout.core.domain.model.relationship.Relationship.default(actor.id, targetId)
            )

        val relationship1 = (
            relationshipRepository.findByActorIdAndTargetId(targetId, actor.id)
                ?: dev.usbharu.hideout.core.domain.model.relationship.Relationship.default(targetId, actor.id)
            )

        val actorInstanceRelationship =
            actorInstanceRelationshipRepository.findByActorIdAndInstanceId(actor.id, target.instance)
                ?: ActorInstanceRelationship.default(
                    actor.id,
                    target.instance
                )

        return Relationship.of(relationship, relationship1, actorInstanceRelationship)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetRelationshipApplicationService::class.java)
    }
}

data class GetRelationship(val targetActorId: Long)
