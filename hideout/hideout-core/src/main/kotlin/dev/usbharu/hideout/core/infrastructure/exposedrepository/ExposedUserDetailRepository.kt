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
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailHashedPassword
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedUserDetailRepository(override val domainEventPublisher: DomainEventPublisher) :
    UserDetailRepository,
    AbstractRepository(logger),
    DomainEventPublishableRepository<UserDetail> {

    override suspend fun save(userDetail: UserDetail): UserDetail = query {
        UserDetails.upsert {
            it[id] = userDetail.id.id
            it[actorId] = userDetail.actorId.id
            it[password] = userDetail.password.password
            it[autoAcceptFolloweeFollowRequest] = userDetail.autoAcceptFolloweeFollowRequest
            it[lastMigration] = userDetail.lastMigration
            it[homeTimelineId] = userDetail.homeTimelineId?.value
        }

        onComplete {
            update(userDetail)
        }
        userDetail
    }

    override suspend fun delete(userDetail: UserDetail) {
        query {
            UserDetails.deleteWhere { id eq userDetail.id.id }
            onComplete {
                update(userDetail)
            }
        }
    }

    override suspend fun findByActorId(actorId: Long): UserDetail? = query {
        return@query UserDetails
            .selectAll().where { UserDetails.actorId eq actorId }
            .limit(1)
            .singleOrNull()
            ?.let {
                userDetail(it)
            }
    }

    override suspend fun findById(id: UserDetailId): UserDetail? = query {
        UserDetails
            .selectAll().where { UserDetails.id eq id.id }
            .limit(1)
            .singleOrNull()
            ?.let {
                userDetail(it)
            }
    }

    override suspend fun findAllById(idList: List<UserDetailId>): List<UserDetail> {
        return query {
            UserDetails
                .selectAll()
                .where { UserDetails.id inList idList.map { it.id } }
                .map {
                    userDetail(it)
                }
        }
    }

    private fun userDetail(it: ResultRow) = UserDetail(
        id = UserDetailId(it[UserDetails.id]),
        actorId = ActorId(it[UserDetails.actorId]),
        password = UserDetailHashedPassword(it[UserDetails.password]),
        autoAcceptFolloweeFollowRequest = it[UserDetails.autoAcceptFolloweeFollowRequest],
        lastMigration = it[UserDetails.lastMigration],
        homeTimelineId = it[UserDetails.homeTimelineId]?.let { it1 -> TimelineId(it1) }
    )

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedUserDetailRepository::class.java)
    }
}

object UserDetails : Table("user_details") {
    val id = long("id")
    val actorId = long("actor_id").references(Actors.id)
    val password = varchar("password", 255)
    val autoAcceptFolloweeFollowRequest = bool("auto_accept_followee_follow_request")
    val lastMigration = timestamp("last_migration").nullable()
    val homeTimelineId = long("home_timeline_id").references(Timelines.id).nullable()
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
