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

import dev.usbharu.hideout.core.domain.model.timeline.*
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedTimelineRepository(override val domainEventPublisher: DomainEventPublisher) :
    TimelineRepository,
    AbstractRepository(logger),
    DomainEventPublishableRepository<Timeline> {

    override suspend fun save(timeline: Timeline): Timeline {
        query {
            Timelines.upsert {
                it[id] = timeline.id.value
                it[userDetailId] = timeline.userDetailId.id
                it[name] = timeline.name.value
                it[visibility] = timeline.visibility.name
                it[isSystem] = timeline.isSystem
            }
            onComplete {
                update(timeline)
            }
        }

        return timeline
    }

    override suspend fun delete(timeline: Timeline) {
        query {
            Timelines.deleteWhere {
                Timelines.id eq timeline.id.value
            }
            onComplete {
                update(timeline)
            }
        }
    }

    override suspend fun findByIds(ids: List<TimelineId>): List<Timeline> {
        return query {
            Timelines.selectAll().where { Timelines.id inList ids.map { it.value } }.map { it.toTimeline() }
        }
    }

    override suspend fun findById(id: TimelineId): Timeline? {
        return query {
            Timelines.selectAll().where { Timelines.id eq id.value }.limit(1).firstOrNull()?.toTimeline()
        }
    }

    override suspend fun findAllByUserDetailIdAndVisibilityIn(
        userDetailId: UserDetailId,
        visibility: List<TimelineVisibility>
    ): List<Timeline> {
        return query {
            Timelines.selectAll().where {
                Timelines.userDetailId eq userDetailId.id and (Timelines.visibility inList visibility.map { it.name })
            }.map { it.toTimeline() }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedTimelineRepository::class.java.name)
    }
}

fun ResultRow.toTimeline(): Timeline {
    return Timeline(
        TimelineId(this[Timelines.id]),
        UserDetailId(this[Timelines.userDetailId]),
        TimelineName(this[Timelines.name]),
        TimelineVisibility.valueOf(this[Timelines.visibility]),
        this[Timelines.isSystem]
    )
}

object Timelines : Table("timelines") {
    val id = long("id")
    val userDetailId = long("user_detail_id").references(UserDetails.id)
    val name = varchar("name", 300)
    val visibility = varchar("visibility", 100)
    val isSystem = bool("is_system")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
