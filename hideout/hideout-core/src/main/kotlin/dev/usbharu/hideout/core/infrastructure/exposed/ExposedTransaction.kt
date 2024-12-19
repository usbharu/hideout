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

import dev.usbharu.hideout.core.application.shared.Transaction
import kotlinx.coroutines.slf4j.MDCContext
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Component
import java.sql.Connection

@Component
class ExposedTransaction : Transaction {
    override suspend fun <T> transaction(block: suspend () -> T): T {
        return newSuspendedTransaction(
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED,
            context = MDCContext()
        ) {
            debug = true
            warnLongQueriesDuration = 1000
            addLogger(Slf4jSqlDebugLogger)
            block()
        }
    }

    override suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T {
        return newSuspendedTransaction(MDCContext(), transactionIsolation = transactionLevel) {
            addLogger(Slf4jSqlDebugLogger)
            block()
        }
    }
}
