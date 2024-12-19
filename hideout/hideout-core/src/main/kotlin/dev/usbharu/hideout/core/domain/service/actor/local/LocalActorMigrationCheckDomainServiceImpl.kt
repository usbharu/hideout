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

package dev.usbharu.hideout.core.domain.service.actor.local

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

@Service
class LocalActorMigrationCheckDomainServiceImpl : LocalActorMigrationCheckDomainService {
    override suspend fun canAccountMigration(userDetail: UserDetail, from: Actor, to: Actor): AccountMigrationCheck {
        val lastMigration = userDetail.lastMigration
        if (lastMigration != null) {
            val instant = lastMigration.plus(30.days.toJavaDuration())
            if (instant.isAfter(Instant.now())) {
                return AccountMigrationCheck.MigrationCoolDown("You can migration at $instant.")
            }
        }

        if (to == from) {
            return AccountMigrationCheck.SelfReferences()
        }

        if (to.moveTo != null) {
            return AccountMigrationCheck.AlreadyMoved("${to.name}@${to.domain} was move to ${to.moveTo}")
        }

        if (from.moveTo != null) {
            return AccountMigrationCheck.AlreadyMoved("${from.name}@${from.domain} was move to ${from.moveTo}")
        }

        if (to.alsoKnownAs.contains(from.id).not()) {
            return AccountMigrationCheck.AlsoKnownAsNotFound("${to.id} has ${to.alsoKnownAs}")
        }

        return AccountMigrationCheck.CanAccountMigration()
    }
}
