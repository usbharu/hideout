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

package dev.usbharu.hideout.mastodon.infrastructure.exposedquery

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Account
import dev.usbharu.hideout.mastodon.query.AccountQueryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository

@Repository
class ExposedAccountQueryServiceImpl(private val applicationConfig: ApplicationConfig) : AccountQueryService {
    override suspend fun findById(accountId: Long): Account? {
        val query = Actors.selectAll().where { Actors.id eq accountId }

        return query
            .singleOrNull()
            ?.let { toAccount(it) }
    }

    override suspend fun findByIds(accountIds: List<Long>): List<Account> {
        val query = Actors.selectAll().where { Actors.id inList accountIds }

        return query
            .map { toAccount(it) }
    }

    private fun toAccount(
        resultRow: ResultRow,
    ): Account {
        val userUrl = "${applicationConfig.url}/users/${resultRow[Actors.id]}"

        return Account(
            id = resultRow[Actors.id].toString(),
            username = resultRow[Actors.name],
            acct = "${resultRow[Actors.name]}@${resultRow[Actors.domain]}",
            url = resultRow[Actors.url].toString(),
            displayName = resultRow[Actors.screenName],
            note = resultRow[Actors.description],
            avatar = "$userUrl/icon.jpg",
            avatarStatic = "$userUrl/icon.jpg",
            header = "$userUrl/header.jpg",
            headerStatic = "$userUrl/header.jpg",
            locked = resultRow[Actors.locked],
            fields = emptyList(),
            emojis = emptyList(),
            bot = false,
            group = false,
            discoverable = true,
            createdAt = resultRow[Actors.createdAt].toString(),
            lastStatusAt = resultRow[Actors.lastPostAt]?.toString(),
            statusesCount = resultRow[Actors.postsCount],
            followersCount = resultRow[Actors.followersCount],
            followingCount = resultRow[Actors.followingCount],
            noindex = false,
            moved = false,
            suspended = resultRow[Actors.suspend],
            limited = false,
        )
    }
}
