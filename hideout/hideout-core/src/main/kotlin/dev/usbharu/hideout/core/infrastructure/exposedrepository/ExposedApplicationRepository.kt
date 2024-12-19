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

import dev.usbharu.hideout.core.domain.model.application.Application
import dev.usbharu.hideout.core.domain.model.application.ApplicationRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.upsert
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedApplicationRepository : ApplicationRepository, AbstractRepository(logger) {

    override suspend fun save(application: Application) = query {
        Applications.upsert {
            it[id] = application.applicationId.id
            it[name] = application.name.name
        }
        application
    }

    override suspend fun delete(application: Application): Unit = query {
        Applications.deleteWhere { id eq application.applicationId.id }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedApplicationRepository::class.java)
    }
}

object Applications : Table("applications") {
    val id = long("id")
    val name = varchar("name", 500)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
