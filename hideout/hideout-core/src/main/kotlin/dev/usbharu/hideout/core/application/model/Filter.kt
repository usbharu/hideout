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

package dev.usbharu.hideout.core.application.model

import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterAction
import dev.usbharu.hideout.core.domain.model.filter.FilterContext

data class Filter(
    val filterId: Long,
    val userDetailId: Long,
    val name: String,
    val filterContext: Set<FilterContext>,
    val filterAction: FilterAction,
    val filterKeywords: Set<FilterKeyword>,
) {
    companion object {
        fun of(filter: Filter): dev.usbharu.hideout.core.application.model.Filter {
            return Filter(
                filterId = filter.id.id,
                userDetailId = filter.userDetailId.id,
                name = filter.name.name,
                filterContext = filter.filterContext,
                filterAction = filter.filterAction,
                filterKeywords = filter.filterKeywords.map {
                    FilterKeyword(
                        it.id.id,
                        it.keyword.keyword,
                        it.mode
                    )
                }.toSet()
            )
        }
    }
}
