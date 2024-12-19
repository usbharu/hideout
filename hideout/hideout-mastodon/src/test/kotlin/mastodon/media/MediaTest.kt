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

package mastodon.media

import dev.usbharu.hideout.SpringApplication
import kotlinx.coroutines.test.runTest
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/actors.sql", "/sql/userdetail.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class MediaTest {

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
    fun メディアをアップロードできる() = runTest {
        mockMvc
            .multipart("/api/v1/media") {
                file(
                    MockMultipartFile(
                        "file",
                        "400x400.png",
                        "image/png",
                        String.javaClass.classLoader.getResourceAsStream("media/400x400.png")
                    )
                )
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun write_mediaスコープでメディアをアップロードできる() = runTest {
        mockMvc
            .multipart("/api/v1/media") {
                file(
                    MockMultipartFile(
                        "file",
                        "400x400.png",
                        "image/png",
                        String.javaClass.classLoader.getResourceAsStream("media/400x400.png")
                    )
                )
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:media")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun 権限がないと403() = runTest {
        mockMvc
            .multipart("/api/v1/media") {
                file(
                    MockMultipartFile(
                        "file",
                        "400x400.png",
                        "image/png",
                        String.javaClass.classLoader.getResourceAsStream("media/400x400.png")
                    )
                )
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
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
