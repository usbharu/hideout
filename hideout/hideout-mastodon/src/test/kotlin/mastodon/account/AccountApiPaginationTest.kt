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

package mastodon.account

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/actors.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql("/sql/accounts/test-accounts-statuses.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Disabled
class AccountApiPaginationTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @Test
    fun `apiV1AccountsIdStatusesGet 投稿を取得できる`() {
        val content = mockMvc
            .get("/api/v1/accounts/1/statuses") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect {
                header {
                    string(
                        "Link",
                        "<https://example.com/api/v1/accounts/1/statuses?min_id=100>; rel=\"next\", <https://example.com/api/v1/accounts/1/statuses?max_id=81>; rel=\"prev\""
                    )
                }
            }
            .andReturn()
            .response
            .contentAsString

        val value: List<Status> =
            jacksonObjectMapper().readValue<List<Status>>(content, object : TypeReference<List<Status>>() {})

        assertThat(value.first().id).isEqualTo("100")
        assertThat(value.last().id).isEqualTo("81")
        assertThat(value).size().isEqualTo(20)
    }

    @Test
    fun `apiV1AccountsIdStatusesGet 結果が0件のときはLinkヘッダーがない`() {
        val content = mockMvc
            .get("/api/v1/accounts/1/statuses?min_id=100") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andDo { print() }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { header { doesNotExist("Link") } }
            .andReturn()
            .response
            .contentAsString

        val value = jacksonObjectMapper().readValue(content, object : TypeReference<List<Status>>() {})

        assertThat(value).isEmpty()
    }

    @Test
    fun `apiV1AccountsIdStatusesGet maxIdを指定して取得`() {
        val content = mockMvc
            .get("/api/v1/accounts/1/statuses?max_id=100") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect {
                header {
                    string(
                        "Link",
                        "<https://example.com/api/v1/accounts/1/statuses?min_id=99>; rel=\"next\", <https://example.com/api/v1/accounts/1/statuses?max_id=80>; rel=\"prev\""
                    )
                }
            }
            .andReturn()
            .response
            .contentAsString

        val value = jacksonObjectMapper().readValue(content, object : TypeReference<List<Status>>() {})

        assertThat(value.first().id).isEqualTo("99")
        assertThat(value.last().id).isEqualTo("80")
        assertThat(value).size().isEqualTo(20)
    }

    @Test
    fun `apiV1AccountsIdStatusesGet minIdを指定して取得`() {
        val content = mockMvc
            .get("/api/v1/accounts/1/statuses?min_id=1") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect {
                header {
                    string(
                        "Link",
                        "<https://example.com/api/v1/accounts/1/statuses?min_id=21>; rel=\"next\", <https://example.com/api/v1/accounts/1/statuses?max_id=2>; rel=\"prev\""
                    )
                }
            }
            .andReturn()
            .response
            .contentAsString

        val value = jacksonObjectMapper().readValue(content, object : TypeReference<List<Status>>() {})

        assertThat(value.first().id).isEqualTo("21")
        assertThat(value.last().id).isEqualTo("2")
        assertThat(value).size().isEqualTo(20)
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
