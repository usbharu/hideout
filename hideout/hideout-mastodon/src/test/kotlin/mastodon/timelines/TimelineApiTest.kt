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

package mastodon.timelines

import dev.usbharu.hideout.SpringApplication
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@Transactional
@Sql("/sql/actors.sql", "/sql/userdetail.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class TimelineApiTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    fun `apiV1TimelinesHomeGetにreadでアクセスできる`() {
        mockMvc
            .get("/api/v1/timelines/home") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1TimelinesHomeGetにread statusesでアクセスできる`() {
        mockMvc
            .get("/api/v1/timelines/home") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:statuses"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1TimelineHomeGetに匿名でアクセスすると401() {
        mockMvc
            .get("/api/v1/timelines/home")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun apiV1TimelinesPublicGetにreadでアクセスできる() {
        mockMvc
            .get("/api/v1/timelines/public") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1TimelinesPublicGetにread statusesでアクセスできる`() {
        mockMvc
            .get("/api/v1/timelines/public") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:statuses"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1TimeinesPublicGetに匿名でアクセスできる() {
        mockMvc
            .get("/api/v1/timelines/public")
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun dropDatabase(@Autowired flyway: Flyway) {
            flyway.clean()
            flyway.migrate()
        }
    }
}
