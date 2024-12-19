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

package dev.usbharu.hideout.core.domain.model.support.domain

import java.net.URI

@JvmInline
value class Domain(val domain: String) {
    init {
        require(domain.length <= LENGTH)
    }

    companion object {
        const val LENGTH: Int = 1000

        fun of(uri: URI): Domain = Domain(uri.apHost)
    }
}

val URI.apHost: String
    get() = if (port == -1) {
        host
    } else {
        "$host:$port"
    }
