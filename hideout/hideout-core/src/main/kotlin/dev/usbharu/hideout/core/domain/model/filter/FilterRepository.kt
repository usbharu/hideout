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

package dev.usbharu.hideout.core.domain.model.filter

import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

interface FilterRepository {
    suspend fun save(filter: Filter): Filter
    suspend fun delete(filter: Filter)

    suspend fun findByFilterKeywordId(filterKeywordId: FilterKeywordId): Filter?
    suspend fun findByFilterId(filterId: FilterId): Filter?
    suspend fun findByUserDetailId(userDetailId: UserDetailId): List<Filter>
}
