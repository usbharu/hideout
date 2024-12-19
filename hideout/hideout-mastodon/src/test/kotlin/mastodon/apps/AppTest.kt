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

package mastodon.apps

import dev.usbharu.hideout.SpringApplication
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import util.objectMapper
import kotlin.test.assertNotNull

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
class AppTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jdbcOperations: JdbcOperations


    val registeredClientRepository: JdbcRegisteredClientRepository by lazy {
        JdbcRegisteredClientRepository(
            jdbcOperations
        )
    }

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    @WithAnonymousUser
    fun apiV1AppsPostにformで匿名でappを作成できる() {
        val contentAsString = mockMvc
            .post("/api/v1/apps") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("client_name", "test-client")
                param("redirect_uris", "https://example.com")
                param("scopes", "write read")
                param("website", "https://example.com")
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        val clientId = objectMapper().readTree(contentAsString)["client_id"].asText()


        val registeredClient = registeredClientRepository.findByClientId(clientId)

        assertNotNull(registeredClient)
        assertThat(registeredClient.clientName).isEqualTo("test-client")
        assertThat(registeredClient.redirectUris.joinToString(",")).isEqualTo("https://example.com")
        assertThat(registeredClient.scopes.joinToString(",")).isEqualTo("read,write")
    }

    @Test
    @WithAnonymousUser
    fun apiV1AppsPostにjsonで匿名でappを作成できる() {
        val contentAsString = mockMvc
            .post("/api/v1/apps") {
                contentType = MediaType.APPLICATION_JSON
                content = """{
  "client_name": "test-client-2",
  "redirect_uris": "https://example.com",
  "scopes": "write read",
  "website": "https;//example.com"
}"""
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        val clientId = objectMapper().readTree(contentAsString)["client_id"].asText()


        val registeredClient = registeredClientRepository.findByClientId(clientId)

        assertNotNull(registeredClient)
        assertThat(registeredClient.clientName).isEqualTo("test-client-2")
        assertThat(registeredClient.redirectUris.joinToString(",")).isEqualTo("https://example.com")
        assertThat(registeredClient.scopes.joinToString(",")).isEqualTo("read,write")
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
