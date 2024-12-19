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

package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.AbstractRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.core.infrastructure.exposedrepository.UserDetails
import dev.usbharu.hideout.core.query.principal.PrincipalDTO
import dev.usbharu.hideout.core.query.principal.PrincipalQueryService
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedPrincipalQueryService : PrincipalQueryService, AbstractRepository(logger) {

    override suspend fun findByUserDetailId(userDetailId: UserDetailId): PrincipalDTO {
        return query {
            UserDetails.leftJoin(Actors).selectAll().where { UserDetails.id eq userDetailId.id }.single()
                .let {
                    PrincipalDTO(
                        UserDetailId(it[UserDetails.id]),
                        ActorId(it[UserDetails.actorId]),
                        it[Actors.name],
                        it[Actors.domain]
                    )
                }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ExposedPrincipalQueryService::class.java)
    }
}
