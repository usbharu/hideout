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

package dev.usbharu.hideout.mastodon.query

import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status

interface StatusQueryService {
    suspend fun findByPostIds(ids: List<Long>): List<Status>
    suspend fun findByPostIdsWithMediaIds(statusQueries: List<StatusQuery>): List<Status>

    @Suppress("LongParameterList")
    suspend fun accountsStatus(
        accountId: Long,
        onlyMedia: Boolean = false,
        excludeReplies: Boolean = false,
        excludeReblogs: Boolean = false,
        pinned: Boolean = false,
        tagged: String?,
        includeFollowers: Boolean = false,
    ): List<Status>

    suspend fun findByPostId(id: Long, principal: Principal? = null): Status?
}

data class StatusQuery(
    val postId: Long,
    val replyId: Long?,
    val repostId: Long?,
    val mediaIds: List<Long>,
    val emojiIds: List<Long>,
)
