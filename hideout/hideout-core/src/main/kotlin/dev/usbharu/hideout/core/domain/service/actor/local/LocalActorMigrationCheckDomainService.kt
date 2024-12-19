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

interface LocalActorMigrationCheckDomainService {
    suspend fun canAccountMigration(userDetail: UserDetail, from: Actor, to: Actor): AccountMigrationCheck
}

sealed class AccountMigrationCheck(
    val canMigration: Boolean,
) {
    class CanAccountMigration : AccountMigrationCheck(true)

    class CircularReferences(val message: String) : AccountMigrationCheck(false)

    class SelfReferences : AccountMigrationCheck(false)

    class AlreadyMoved(val message: String) : AccountMigrationCheck(false)

    class AlsoKnownAsNotFound(val message: String) : AccountMigrationCheck(false)

    class MigrationCoolDown(val message: String) : AccountMigrationCheck(false)
}
