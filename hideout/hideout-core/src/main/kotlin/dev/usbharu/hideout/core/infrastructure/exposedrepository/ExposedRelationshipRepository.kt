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
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedRelationshipRepository(override val domainEventPublisher: DomainEventPublisher) :
    RelationshipRepository,
    AbstractRepository(logger),
    DomainEventPublishableRepository<Relationship> {

    override suspend fun save(relationship: Relationship): Relationship {
        query {
            Relationships.upsert {
                it[actorId] = relationship.actorId.id
                it[targetActorId] = relationship.targetActorId.id
                it[following] = relationship.following
                it[blocking] = relationship.blocking
                it[muting] = relationship.muting
                it[followRequesting] = relationship.followRequesting
                it[mutingFollowRequest] = relationship.mutingFollowRequest
            }
            onComplete {
                update(relationship)
            }
        }

        return relationship
    }

    override suspend fun delete(relationship: Relationship) {
        query {
            Relationships.deleteWhere {
                actorId eq relationship.actorId.id and (targetActorId eq relationship.targetActorId.id)
            }
            onComplete {
                update(relationship)
            }
        }
    }

    override suspend fun findByActorIdAndTargetId(actorId: ActorId, targetId: ActorId): Relationship? = query {
        Relationships.selectAll().where {
            Relationships.actorId eq actorId.id and (Relationships.targetActorId eq targetId.id)
        }.limit(1).singleOrNull()?.toRelationships()
    }

    override suspend fun findByActorIdsAndTargetIdAndBlocking(
        actorIds: List<ActorId>,
        targetId: ActorId,
        blocking: Boolean
    ): List<Relationship> = query {
        Relationships.selectAll().where {
            Relationships.actorId inList actorIds.map {
                it.id
            } and (Relationships.targetActorId eq targetId.id) and (Relationships.blocking eq blocking)
        }.map { it.toRelationships() }
    }

    override suspend fun findByActorIdAndTargetIdsAndFollowing(
        actorId: ActorId,
        targetIds: List<ActorId>,
        following: Boolean
    ): List<Relationship> = query {
        Relationships.selectAll().where {
            Relationships.actorId eq actorId.id and (
                Relationships.targetActorId inList targetIds.map {
                    it.id
                }
                ) and (Relationships.following eq following)
        }.map { it.toRelationships() }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedRelationshipRepository::class.java)
    }
}

fun ResultRow.toRelationships(): Relationship = Relationship(
    actorId = ActorId(this[Relationships.actorId]),
    targetActorId = ActorId(this[Relationships.targetActorId]),
    following = this[Relationships.following],
    blocking = this[Relationships.blocking],
    muting = this[Relationships.muting],
    followRequesting = this[Relationships.followRequesting],
    mutingFollowRequest = this[Relationships.mutingFollowRequest]
)

object Relationships : Table("relationships") {
    val actorId = long("actor_id").references(Actors.id)
    val targetActorId = long("target_actor_id").references(Actors.id)
    val following = bool("following")
    val blocking = bool("blocking")
    val muting = bool("muting")
    val followRequesting = bool("follow_requesting")
    val mutingFollowRequest = bool("muting_follow_request")

    override val primaryKey: PrimaryKey = PrimaryKey(actorId, targetActorId)
}
