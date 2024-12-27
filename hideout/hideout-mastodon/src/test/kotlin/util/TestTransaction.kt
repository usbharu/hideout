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

package util

import dev.usbharu.hideout.core.application.shared.Transaction

object TestTransaction : Transaction {
    override suspend fun <T> transaction(block: suspend () -> T): T = block()

    override suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T = block()
}