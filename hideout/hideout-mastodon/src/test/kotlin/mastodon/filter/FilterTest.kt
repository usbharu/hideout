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

package mastodon.filter

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.FilterKeywordsPostRequest
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.FilterPostRequest
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.FilterPostRequestKeyword
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.V1FilterPostRequest
import kotlinx.coroutines.test.runTest
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import util.objectMapper

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
@Sql(
    "/sql/actors.sql",
    "/sql/userdetail.sql",
    "/sql/filter/test-filter.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
class FilterTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    fun `apiV2FiltersPost write権限で追加できる`() {
        mockMvc
            .post("/api/v2/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    FilterPostRequest(
                        title = "mute test",
                        context = listOf(FilterPostRequest.Context.home, FilterPostRequest.Context.public),
                        filterAction = FilterPostRequest.FilterAction.warn,
                        expiresIn = null,
                        keywordsAttributes = listOf(
                            FilterPostRequestKeyword(
                                keyword = "hoge",
                                wholeWord = false,
                                regex = true
                            )
                        )
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect {
                content {
                    jsonPath("$.keywords[0].keyword") {
                        value("hoge")
                    }
                }
            }
    }

    @Test
    fun `apiV2FiltersPost write_filters権限で追加できる`() {
        mockMvc
            .post("/api/v2/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    FilterPostRequest(
                        title = "mute test",
                        context = listOf(FilterPostRequest.Context.home, FilterPostRequest.Context.public),
                        filterAction = FilterPostRequest.FilterAction.warn,
                        expiresIn = null,
                        keywordsAttributes = listOf(
                            FilterPostRequestKeyword(
                                keyword = "fuga",
                                wholeWord = true,
                                regex = false
                            )
                        )
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect {
                content {
                    jsonPath("$.keywords[0].keyword") {
                        value("fuga")
                    }
                }
            }
    }

    @Test
    fun `apiV2FiltersPost read権限で401`() {
        mockMvc
            .post("/api/v2/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    FilterPostRequest(
                        title = "mute test",
                        context = listOf(FilterPostRequest.Context.home, FilterPostRequest.Context.public),
                        filterAction = FilterPostRequest.FilterAction.warn,
                        expiresIn = null,
                        keywordsAttributes = listOf(
                            FilterPostRequestKeyword(
                                keyword = "fuga",
                                wholeWord = true,
                                regex = false
                            )
                        )
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersGet read権限で取得できる`() {
        mockMvc
            .get("/api/v2/filters") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersGet read_filters権限で取得できる`() {
        mockMvc
            .get("/api/v2/filters") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersGet write権限で401`() {
        mockMvc
            .get("/api/v2/filters") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersIdGet read権限で取得できる`() {
        mockMvc
            .get("/api/v2/filters/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }


    @Test
    fun `apiV2FiltersIdGet read_filters権限で取得できる`() {
        mockMvc
            .get("/api/v2/filters/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersIdGet write権限で401`() {
        mockMvc
            .get("/api/v2/filters/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersFilterIdKeywordsGet read権限で取得できる`() {
        mockMvc
            .get("/api/v2/filters/1/keywords") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersFilterIdKeywordsGet read_filters権限で取得できる`() {
        mockMvc
            .get("/api/v2/filters/1/keywords") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersFilterIdKeywordsGet writeで403`() {
        mockMvc
            .get("/api/v2/filters/1/keywords") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersFilterIdKeywordsPost writeで追加できる`() {
        mockMvc
            .post("/api/v2/filters/1/keywords") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    FilterKeywordsPostRequest(
                        "hage", false, false
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersFilterIdKeywordsPost write_filtersで追加できる`() {
        mockMvc
            .post("/api/v2/filters/1/keywords") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    FilterKeywordsPostRequest(
                        "hage", false, false
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersFilterIdKeywordsPost readで403`() {
        mockMvc
            .post("/api/v2/filters/1/keywords") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    FilterKeywordsPostRequest(
                        "hage", false, false
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersKeywordsIdGet readで取得できる`() {
        mockMvc
            .get("/api/v2/filters/keywords/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersKeywordsIdGet read_filtersで取得できる`() {
        mockMvc
            .get("/api/v2/filters/keywords/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersKeywordsIdGet writeだと403`() {
        mockMvc
            .get("/api/v2/filters/keywords/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersKeyowrdsIdDelete writeで削除できる`() = runTest {
        mockMvc
            .delete("/api/v2/filters/keywords/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersKeyowrdsIdDelete write_filtersで削除できる`() = runTest {
        mockMvc
            .delete("/api/v2/filters/keywords/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersKeyowrdsIdDelete readで403`() = runTest {
        mockMvc
            .delete("/api/v2/filters/keywords/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersFilterIdStatuses readで取得できる`() {
        mockMvc
            .get("/api/v2/filters/1/statuses") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `apiV2FiltersFilterIdStatuses read_filtersで取得できる`() {
        mockMvc
            .get("/api/v2/filters/1/statuses") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `apiV2FiltersFilterIdStatuses writeで403`() {
        mockMvc
            .get("/api/v2/filters/1/statuses") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersStatusesIdGet readで取得できる`() {
        mockMvc
            .get("/api/v2/filters/statuses/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `apiV2FiltersStatusesIdGet read_filtersで取得できる`() {
        mockMvc
            .get("/api/v2/filters/statuses/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `apiV2FiltersStatusesIdGet writeで403`() {
        mockMvc
            .get("/api/v2/filters/statuses/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersStatusesIdDelete writeで削除できる`() {
        mockMvc
            .delete("/api/v2/filters/statuses/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `apiV2FiltersStatusesIdDelete write_filtersで削除できる`() {
        mockMvc
            .delete("/api/v2/filters/statuses/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `apiV2FiltersStatusesIdDelete readで403`() {
        mockMvc
            .delete("/api/v2/filters/statuses/1") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV1FiltersGet readで取得できる`() {
        mockMvc
            .get("/api/v1/filters") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1FiltersGet read_filtersで取得できる`() {
        mockMvc
            .get("/api/v1/filters") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1FiltersGet writeで403`() {
        mockMvc
            .get("/api/v1/filters") {
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV1FiltersPost writeで新規作成`() {
        mockMvc
            .post("/api/v1/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    V1FilterPostRequest(
                        phrase = "hoge",
                        context = listOf(V1FilterPostRequest.Context.home),
                        irreversible = false,
                        wholeWord = false,
                        expiresIn = null
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1FiltersPost write_filtersで新規作成`() {
        mockMvc
            .post("/api/v1/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    V1FilterPostRequest(
                        phrase = "hoge",
                        context = listOf(V1FilterPostRequest.Context.home),
                        irreversible = false,
                        wholeWord = false,
                        expiresIn = null
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1FiltersPost readで403`() {
        mockMvc
            .post("/api/v1/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper().writeValueAsString(
                    V1FilterPostRequest(
                        phrase = "hoge",
                        context = listOf(V1FilterPostRequest.Context.home),
                        irreversible = false,
                        wholeWord = false,
                        expiresIn = null
                    )
                )
                with(
                    jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV1FiltersIdGet readで取得できる`() {
        mockMvc
            .get("/api/v1/filters/1") {
                with(
                    jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1FiltersIdGet read_filtersで取得できる`() {
        mockMvc
            .get("/api/v1/filters/1") {
                with(
                    jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1FiltersIdGet writeで403`() {
        mockMvc
            .get("/api/v1/filters/1") {
                with(
                    jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    @Sql("/sql/filter/test-filter.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `apiV1FiltersIdDelete writeで削除できる`() {
        mockMvc
            .delete("/api/v1/filters/1") {
                with(
                    jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    @Sql("/sql/filter/test-filter.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `apiV1FiltersIdDelete write_filtersで削除できる`() {
        mockMvc
            .delete("/api/v1/filters/1") {
                with(
                    jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1FiltersIdDelete readで403`() {
        mockMvc
            .delete("/api/v1/filters/1") {
                with(
                    jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andExpect { status { isForbidden() } }
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