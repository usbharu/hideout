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

import dev.usbharu.hideout.core.domain.model.filter.*
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Filters
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component

@Component
class FilterResultRowMapper : ResultRowMapper<Filter> {
    override fun map(resultRow: ResultRow): Filter = Filter(
        id = FilterId(resultRow[Filters.id]),
        userDetailId = UserDetailId(resultRow[Filters.userId]),
        name = FilterName(resultRow[Filters.name]),
        filterContext = resultRow[Filters.context].split(",").filter {
            it.isNotEmpty()
        }.map { FilterContext.valueOf(it) }.toSet(),
        filterAction = FilterAction.valueOf(resultRow[Filters.filterAction]),
        filterKeywords = emptySet()
    )
}
