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

import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterId
import dev.usbharu.hideout.core.domain.model.filter.FilterKeywordId
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.exposed.QueryMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedFilterRepository(private val filterQueryMapper: QueryMapper<Filter>) : FilterRepository,
    AbstractRepository(logger) {

    override suspend fun save(filter: Filter): Filter = query {
        Filters.upsert { upsertStatement ->
            upsertStatement[id] = filter.id.id
            upsertStatement[userId] = filter.userDetailId.id
            upsertStatement[name] = filter.name.name
            upsertStatement[context] = filter.filterContext.joinToString(",") { it.name }
            upsertStatement[filterAction] = filter.filterAction.name
        }
        FilterKeywords.deleteWhere {
            filterId eq filter.id.id
        }
        FilterKeywords.batchUpsert(filter.filterKeywords) {
            this[FilterKeywords.id] = it.id.id
            this[FilterKeywords.filterId] = filter.id.id
            this[FilterKeywords.keyword] = it.keyword.keyword
            this[FilterKeywords.mode] = it.mode.name
        }
        filter
    }

    override suspend fun delete(filter: Filter): Unit = query {
        FilterKeywords.deleteWhere { filterId eq filter.id.id }
        Filters.deleteWhere { id eq filter.id.id }
    }

    override suspend fun findByFilterKeywordId(filterKeywordId: FilterKeywordId): Filter? = query {
        val filterId = FilterKeywords
            .selectAll()
            .where { FilterKeywords.id eq filterKeywordId.id }
            .limit(1)
            .firstOrNull()?.get(FilterKeywords.filterId) ?: return@query null
        val where = Filters.leftJoin(FilterKeywords).selectAll().where { Filters.id eq filterId }
        return@query filterQueryMapper.map(where).firstOrNull()
    }

    override suspend fun findByFilterId(filterId: FilterId): Filter? = query {
        val where = Filters.leftJoin(FilterKeywords).selectAll().where { Filters.id eq filterId.id }
        return@query filterQueryMapper.map(where).firstOrNull()
    }

    override suspend fun findByUserDetailId(userDetailId: UserDetailId): List<Filter> = query {
        return@query Filters.leftJoin(FilterKeywords).selectAll().where { Filters.userId eq userDetailId.id }
            .let(filterQueryMapper::map)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedFilterRepository::class.java)
    }
}

object Filters : Table("filters") {
    val id = long("id")
    val userId = long("user_id").references(UserDetails.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val context = varchar("context", 500)
    val filterAction = varchar("action", 255)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

object FilterKeywords : Table("filter_keywords") {
    val id = long("id")
    val filterId =
        long("filter_id").references(Filters.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val keyword = varchar("keyword", 1000)
    val mode = varchar("mode", 100)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
