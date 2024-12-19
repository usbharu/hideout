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

package dev.usbharu.hideout.core.domain.exception

import dev.usbharu.hideout.core.domain.exception.resource.DuplicateException
import dev.usbharu.hideout.core.domain.exception.resource.ResourceAccessException
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException

class SpringDataAccessExceptionSQLExceptionTranslator : SQLExceptionTranslator {
    override fun translate(message: String, sql: String?, exception: Exception): ResourceAccessException {
        if (exception !is DataAccessException) {
            throw IllegalArgumentException("exception must be DataAccessException.")
        }

        return when (exception) {
            is DuplicateKeyException -> DuplicateException(message, exception.rootCause)
            else -> ResourceAccessException(message, exception.rootCause)
        }
    }
}
