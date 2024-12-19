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

package dev.usbharu.hideout.mastodon.application.accounts

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Account
import dev.usbharu.hideout.mastodon.query.AccountQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetAccountApplicationService(private val accountQueryService: AccountQueryService, transaction: Transaction) :
    AbstractApplicationService<GetAccount, Account>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: GetAccount, principal: Principal): Account {
        return accountQueryService.findById(command.accountId.toLong())
            ?: throw IllegalArgumentException("Account ${command.accountId} not found")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetAccountApplicationService::class.java)
    }
}
