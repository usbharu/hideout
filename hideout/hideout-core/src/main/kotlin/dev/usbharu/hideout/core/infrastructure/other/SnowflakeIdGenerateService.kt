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

package dev.usbharu.hideout.core.infrastructure.other

import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

@Suppress("MagicNumber")
open class SnowflakeIdGenerateService(private val baseTime: Long) : IdGenerateService {
    private var lastTimeStamp: Long = -1
    private var sequenceId: Long = 0
    private val mutex = Mutex()

    @Throws(IllegalStateException::class)
    override suspend fun generateId(): Long {
        return mutex.withLock {
            var timestamp = getTime()
            if (timestamp < lastTimeStamp) {
                timestamp = wait(timestamp)
            }
            if (timestamp == lastTimeStamp) {
                sequenceId++
                if (sequenceId >= 4096) {
                    timestamp = wait(timestamp)
                    sequenceId = 0
                }
            } else {
                sequenceId = 0
            }
            lastTimeStamp = timestamp
            return@withLock (timestamp - baseTime).shl(22).or(1L.shl(12)).or(sequenceId)
        }
    }

    private suspend fun wait(timestamp: Long): Long {
        var timestamp1 = timestamp
        while (timestamp1 <= lastTimeStamp) {
            delay(1L)
            timestamp1 = getTime()
        }
        return timestamp1
    }

    private fun getTime(): Long = Instant.now().toEpochMilli()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnowflakeIdGenerateService

        if (baseTime != other.baseTime) return false
        if (lastTimeStamp != other.lastTimeStamp) return false
        if (sequenceId != other.sequenceId) return false
        if (mutex != other.mutex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = baseTime.hashCode()
        result = 31 * result + lastTimeStamp.hashCode()
        result = 31 * result + sequenceId.hashCode()
        result = 31 * result + mutex.hashCode()
        return result
    }
}
