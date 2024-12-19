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

package dev.usbharu.hideout.core.domain.model.support.page

sealed class Page {
    abstract val maxId: Long?
    abstract val sinceId: Long?
    abstract val minId: Long?
    abstract val limit: Int?

    data class PageByMaxId(
        override val maxId: Long?,
        override val sinceId: Long?,
        override val limit: Int?
    ) : Page() {
        override val minId: Long? = null
    }

    data class PageByMinId(
        override val maxId: Long?,
        override val minId: Long?,
        override val limit: Int?
    ) : Page() {
        override val sinceId: Long? = null
    }

    companion object {
        fun of(
            maxId: Long? = null,
            sinceId: Long? = null,
            minId: Long? = null,
            limit: Int? = null
        ): Page =
            if (minId != null) {
                PageByMinId(
                    maxId,
                    minId,
                    limit
                )
            } else {
                PageByMaxId(
                    maxId,
                    sinceId,
                    limit
                )
            }
    }
}
