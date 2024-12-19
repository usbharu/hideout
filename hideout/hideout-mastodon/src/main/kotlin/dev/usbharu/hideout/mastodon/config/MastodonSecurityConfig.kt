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

package dev.usbharu.hideout.mastodon.config

import dev.usbharu.hideout.mastodon.external.RoleHierarchyAuthorizationManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
class MastodonSecurityConfig {
    @Bean
    @Order(2)
    @Suppress("LongMethod")
    fun mastodonApiSecurityFilterChain(
        http: HttpSecurity,
        rf: RoleHierarchyAuthorizationManagerFactory,
    ): SecurityFilterChain {
        http {
            securityMatcher("/api/v1/**", "/api/v2/**")
            authorizeHttpRequests {
                authorize(POST, "/api/v1/apps", permitAll)
                authorize(GET, "/api/v1/instance/**", permitAll)
                authorize(POST, "/api/v1/accounts", authenticated)

                authorize(GET, "/api/v1/accounts/verify_credentials", rf.hasScope("read:accounts"))
                authorize(GET, "/api/v1/accounts/relationships", rf.hasScope("read:follows"))
                authorize(GET, "/api/v1/accounts/*", permitAll)
                authorize(GET, "/api/v1/accounts/*/statuses", permitAll)
                authorize(POST, "/api/v1/accounts/*/follow", rf.hasScope("write:follows"))
                authorize(POST, "/api/v1/accounts/*/unfollow", rf.hasScope("write:follows"))
                authorize(POST, "/api/v1/accounts/*/block", rf.hasScope("write:blocks"))
                authorize(POST, "/api/v1/accounts/*/unblock", rf.hasScope("write:blocks"))
                authorize(POST, "/api/v1/accounts/*/mute", rf.hasScope("write:mutes"))
                authorize(POST, "/api/v1/accounts/*/unmute", rf.hasScope("write:mutes"))
                authorize(GET, "/api/v1/mutes", rf.hasScope("read:mutes"))

                authorize(POST, "/api/v1/media", rf.hasScope("write:media"))
                authorize(POST, "/api/v1/statuses", rf.hasScope("write:statuses"))
                authorize(GET, "/api/v1/statuses/*", permitAll)
                authorize(POST, "/api/v1/statuses/*/favourite", rf.hasScope("write:favourites"))
                authorize(POST, "/api/v1/statuses/*/unfavourite", rf.hasScope("write:favourites"))
                authorize(PUT, "/api/v1/statuses/*/emoji_reactions/*", rf.hasScope("write:favourites"))

                authorize(GET, "/api/v1/timelines/public", permitAll)
                authorize(GET, "/api/v1/timelines/home", rf.hasScope("read:statuses"))

                authorize(GET, "/api/v2/filters", rf.hasScope("read:filters"))
                authorize(POST, "/api/v2/filters", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/*", rf.hasScope("read:filters"))
                authorize(PUT, "/api/v2/filters/*", rf.hasScope("write:filters"))
                authorize(DELETE, "/api/v2/filters/*", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/*/keywords", rf.hasScope("read:filters"))
                authorize(POST, "/api/v2/filters/*/keywords", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/keywords/*", rf.hasScope("read:filters"))
                authorize(PUT, "/api/v2/filters/keywords/*", rf.hasScope("write:filters"))
                authorize(DELETE, "/api/v2/filters/keywords/*", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/*/statuses", rf.hasScope("read:filters"))
                authorize(POST, "/api/v2/filters/*/statuses", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/statuses/*", rf.hasScope("read:filters"))
                authorize(DELETE, "/api/v2/filters/statuses/*", rf.hasScope("write:filters"))

                authorize(GET, "/api/v1/filters", rf.hasScope("read:filters"))
                authorize(POST, "/api/v1/filters", rf.hasScope("write:filters"))

                authorize(GET, "/api/v1/filters/*", rf.hasScope("read:filters"))
                authorize(POST, "/api/v1/filters/*", rf.hasScope("write:filters"))
                authorize(DELETE, "/api/v1/filters/*", rf.hasScope("write:filters"))

                authorize(GET, "/api/v1/notifications", rf.hasScope("read:notifications"))
                authorize(GET, "/api/v1/notifications/*", rf.hasScope("read:notifications"))
                authorize(POST, "/api/v1/notifications/clear", rf.hasScope("write:notifications"))
                authorize(POST, "/api/v1/notifications/*/dismiss", rf.hasScope("write:notifications"))

                authorize(anyRequest, authenticated)
            }

            oauth2ResourceServer {
                jwt { }
            }

            csrf {
                ignoringRequestMatchers("/api/v1/apps")
            }
        }

        return http.build()
    }
}
