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

package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actorinstancerelationship.ActorInstanceRelationship
import dev.usbharu.hideout.core.domain.model.actorinstancerelationship.ActorInstanceRelationshipRepository
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedActorInstanceRelationshipRepository(override val domainEventPublisher: DomainEventPublisher) :
    ActorInstanceRelationshipRepository,
    AbstractRepository(logger),
    DomainEventPublishableRepository<ActorInstanceRelationship> {

    override suspend fun save(actorInstanceRelationship: ActorInstanceRelationship): ActorInstanceRelationship {
        query {
            ActorInstanceRelationships.upsert {
                it[actorId] = actorInstanceRelationship.actorId.id
                it[instanceId] = actorInstanceRelationship.instanceId.instanceId
                it[blocking] = actorInstanceRelationship.blocking
                it[muting] = actorInstanceRelationship.muting
                it[doNotSendPrivate] = actorInstanceRelationship.doNotSendPrivate
            }
            onComplete {
                update(actorInstanceRelationship)
            }
        }

        return actorInstanceRelationship
    }

    override suspend fun delete(actorInstanceRelationship: ActorInstanceRelationship) {
        query {
            ActorInstanceRelationships.deleteWhere {
                actorId eq actorInstanceRelationship.actorId.id and
                    (instanceId eq actorInstanceRelationship.instanceId.instanceId)
            }
            onComplete {
                update(actorInstanceRelationship)
            }
        }
    }

    override suspend fun findByActorIdAndInstanceId(
        actorId: ActorId,
        instanceId: InstanceId,
    ): ActorInstanceRelationship? = query {
        ActorInstanceRelationships
            .selectAll()
            .where {
                ActorInstanceRelationships.actorId eq actorId.id and
                    (ActorInstanceRelationships.instanceId eq instanceId.instanceId)
            }
            .limit(1)
            .singleOrNull()
            ?.toActorInstanceRelationship()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedActorInstanceRelationshipRepository::class.java)
    }
}

private fun ResultRow.toActorInstanceRelationship(): ActorInstanceRelationship {
    return ActorInstanceRelationship(
        actorId = ActorId(this[ActorInstanceRelationships.actorId]),
        instanceId = InstanceId(this[ActorInstanceRelationships.instanceId]),
        blocking = this[ActorInstanceRelationships.blocking],
        muting = this[ActorInstanceRelationships.muting],
        doNotSendPrivate = this[ActorInstanceRelationships.doNotSendPrivate],
    )
}

object ActorInstanceRelationships : Table("actor_instance_relationships") {
    val actorId = long("actor_id").references(Actors.id)
    val instanceId = long("instance_id").references(Instance.id)
    val blocking = bool("blocking")
    val muting = bool("muting")
    val doNotSendPrivate = bool("do_not_send_private")

    override val primaryKey: PrimaryKey = PrimaryKey(actorId, instanceId)
}
