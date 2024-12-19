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

package dev.usbharu.hideout.core.infrastructure.exposed

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.core.infrastructure.exposedrepository.ActorsAlsoKnownAs
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component

@Component
class ActorQueryMapper(private val actorResultRowMapper: ResultRowMapper<Actor>) : QueryMapper<Actor> {
    override fun map(query: Query): List<Actor> {
        return query
            .groupBy { it[Actors.id] }
            .map { it.value }
            .map {
                it
                    .first()
                    .let(actorResultRowMapper::map)
                    .apply {
                        setAlsoKnownAs(buildAlsoKnownAs(it))
                        clearDomainEvents()
                    }
            }
    }

    private fun buildAlsoKnownAs(it: List<ResultRow>) =
        it.mapNotNull { resultRow: ResultRow ->
            resultRow.getOrNull(
                ActorsAlsoKnownAs.alsoKnownAs
            )?.let { actorId ->
                ActorId(
                    actorId
                )
            }
        }.toSet()
}
