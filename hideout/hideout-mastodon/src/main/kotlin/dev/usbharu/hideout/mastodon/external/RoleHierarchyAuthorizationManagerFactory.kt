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

package dev.usbharu.hideout.mastodon.external

import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.authorization.AuthorityAuthorizationManager
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import org.springframework.stereotype.Component

@Component
class RoleHierarchyAuthorizationManagerFactory(private val roleHierarchy: RoleHierarchy) {
    fun hasScope(role: String): AuthorizationManager<RequestAuthorizationContext> {
        val hasAuthority = AuthorityAuthorizationManager.hasAuthority<RequestAuthorizationContext>("SCOPE_$role")
        hasAuthority.setRoleHierarchy(roleHierarchy)
        return hasAuthority
    }
}
