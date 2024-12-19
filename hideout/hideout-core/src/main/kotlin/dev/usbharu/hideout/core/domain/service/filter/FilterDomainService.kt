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

package dev.usbharu.hideout.core.domain.service.filter

import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterContext
import dev.usbharu.hideout.core.domain.model.filter.FilterResult
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.post.Post
import org.springframework.stereotype.Service

interface IFilterDomainService {
    fun apply(post: Post, context: FilterContext, filters: List<Filter>): FilteredPost
    fun applyAll(postList: List<Post>, context: FilterContext, filters: List<Filter>): List<FilteredPost>
}

@Service
class FilterDomainService : IFilterDomainService {
    override fun apply(post: Post, context: FilterContext, filters: List<Filter>): FilteredPost {
        val filterResults = filters
            .filter { it.filterContext.contains(context) }
            .flatMap { filter ->
                val regex = filter.compileFilter()
                post
                    .overview
                    ?.overview
                    ?.let { it1 -> regex.findAll(it1) }
                    .orEmpty()
                    .toList()
                    .map { FilterResult(filter, it.value) }
                    .plus(
                        post
                            .text
                            .let { regex.findAll(it) }
                            .toList()
                            .map { FilterResult(filter, it.value) }
                    )
            }
        return FilteredPost(post, filterResults)
    }

    override fun applyAll(postList: List<Post>, context: FilterContext, filters: List<Filter>): List<FilteredPost> {
        return filters
            .filter { it.filterContext.contains(context) }
            .map { it to it.compileFilter() }
            .flatMap { compiledFilter ->
                postList
                    .map { post ->
                        val filterResults = post
                            .overview
                            ?.overview
                            ?.let { overview -> compiledFilter.second.findAll(overview) }
                            .orEmpty()
                            .toList()
                            .map { FilterResult(compiledFilter.first, it.value) }
                            .plus(
                                post
                                    .text
                                    .let { compiledFilter.second.findAll(it) }
                                    .toList()
                                    .map { FilterResult(compiledFilter.first, it.value) }
                            )

                        post to filterResults
                    }
            }
            .map { FilteredPost(it.first, it.second) }
    }
}
