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

package dev.usbharu.hideout.core.domain.model.filter

class FilterResult(val filter: Filter, val matchedKeyword: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilterResult

        if (filter != other.filter) return false
        if (matchedKeyword != other.matchedKeyword) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filter.hashCode()
        result = 31 * result + matchedKeyword.hashCode()
        return result
    }

    override fun toString(): String {
        return "FilterResult(" +
            "filter=$filter, " +
            "matchedKeyword='$matchedKeyword'" +
            ")"
    }
}
