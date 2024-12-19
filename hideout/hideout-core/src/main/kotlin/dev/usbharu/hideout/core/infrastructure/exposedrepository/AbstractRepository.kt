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

package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.exception.SpringDataAccessExceptionSQLExceptionTranslator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementInterceptor
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
import java.sql.SQLException

@Suppress("VarCouldBeVal", "UnnecessaryAbstractClass")
abstract class AbstractRepository(protected val logger: Logger) {
    private val sqlErrorCodeSQLExceptionTranslator = SQLErrorCodeSQLExceptionTranslator()
    private val springDataAccessExceptionSQLExceptionTranslator = SpringDataAccessExceptionSQLExceptionTranslator()

    @Value("\${hideout.debug.trace-query-exception:false}")
    private var traceQueryException: Boolean = false

    @Value("\${hideout.debug.trace-query-call:false}")
    private var traceQueryCall: Boolean = false

    class TransactionInterceptor(private val transaction: Transaction) {

        fun onComplete(block: suspend (transaction: Transaction) -> Unit) {
            transaction.registerInterceptor(object : StatementInterceptor {
                override fun afterCommit(transaction: Transaction) {
                    runBlocking(MDCContext()) {
                        block(transaction)
                    }
                }
            })
        }
    }

    protected suspend fun <T> query(block: TransactionInterceptor.() -> T): T = try {
        if (traceQueryCall) {
            @Suppress("ThrowingExceptionsWithoutMessageOrCause")
            logger.trace(
                """
***** QUERY CALL STACK TRACE *****

${Throwable().stackTrace.joinToString("\n")}

***** QUERY CALL STACK TRACE *****
"""
            )
        }

        block.invoke(TransactionInterceptor(TransactionManager.current()))
    } catch (e: SQLException) {
        if (traceQueryException) {
            logger.trace("FAILED EXECUTE SQL", e)
        }
        TransactionManager.currentOrNull()?.rollback()
        if (e.cause !is SQLException) {
            throw e
        }

        val dataAccessException =
            sqlErrorCodeSQLExceptionTranslator.translate("Failed to persist entity", null, e.cause as SQLException)
                ?: throw e

        if (traceQueryException) {
            logger.trace("EXCEPTION TRANSLATED TO", dataAccessException)
        }

        val translate = springDataAccessExceptionSQLExceptionTranslator.translate(
            "Failed to persist entity",
            null,
            dataAccessException
        )

        if (traceQueryException) {
            logger.trace("EXCEPTION TRANSLATED TO", translate)
        }
        throw translate
    }
}
