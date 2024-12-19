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

package mastodon.notifications

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Notification
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class], properties = ["hideout.use-mongodb=true"])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/actors.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
//@Sql("/sql/notification/test-notifications.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class MongodbNotificationsApiPaginationTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @Test
    fun `通知を取得できる`() = runTest {
        val content = mockMvc
            .get("/api/v1/notifications") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect {
                header {
                    string(
                        "Link",
                        "<https://example.com/api/v1/notifications?min_id=65>; rel=\"next\", <https://example.com/api/v1/notifications?max_id=26>; rel=\"prev\""
                    )
                }
            }
            .andReturn()
            .response
            .contentAsString

        val value = jacksonObjectMapper().readValue(content, object : TypeReference<List<Notification>>() {})

        Assertions.assertThat(value.first().id).isEqualTo("65")
        Assertions.assertThat(value.last().id).isEqualTo("26")
    }

    @Test
    fun maxIdを指定して通知を取得できる() = runTest {
        val content = mockMvc
            .get("/api/v1/notifications?max_id=26") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect {
                header {
                    string(
                        "Link",
                        "<https://example.com/api/v1/notifications?min_id=25>; rel=\"next\", <https://example.com/api/v1/notifications?max_id=1>; rel=\"prev\""
                    )
                }
            }
            .andReturn()
            .response
            .contentAsString

        val value = jacksonObjectMapper().readValue(content, object : TypeReference<List<Notification>>() {})

        Assertions.assertThat(value.first().id).isEqualTo("25")
        Assertions.assertThat(value.last().id).isEqualTo("1")

    }

    @Test
    fun minIdを指定して通知を取得できる() = runTest {
        val content = mockMvc
            .get("/api/v1/notifications?min_id=25") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect {
                header {
                    string(
                        "Link",
                        "<https://example.com/api/v1/notifications?min_id=65>; rel=\"next\", <https://example.com/api/v1/notifications?max_id=26>; rel=\"prev\""
                    )
                }
            }
            .andReturn()
            .response
            .contentAsString

        val value = jacksonObjectMapper().readValue(content, object : TypeReference<List<Notification>>() {})

        Assertions.assertThat(value.first().id).isEqualTo("65")
        Assertions.assertThat(value.last().id).isEqualTo("26")
    }

    @Test
    fun 結果が0件のときはページネーションのヘッダーがない() = runTest {
        val content = mockMvc
            .get("/api/v1/notifications?max_id=1") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect {
                header {
                    doesNotExist("Link")
                }
            }
            .andReturn()
            .response
            .contentAsString

        val value = jacksonObjectMapper().readValue(content, object : TypeReference<List<Notification>>() {})

        Assertions.assertThat(value).size().isZero()
    }

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
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