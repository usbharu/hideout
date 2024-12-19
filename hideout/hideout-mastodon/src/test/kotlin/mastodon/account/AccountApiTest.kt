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

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.ExposedRelationshipRepository
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/actors.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class AccountApiTest {

    @Autowired
    private lateinit var actorRepository: ActorRepository

    @Autowired
    lateinit var relationshipRepository: ExposedRelationshipRepository

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `apiV1AccountsVerifyCredentialsGetにreadでアクセスできる`() {
        mockMvc
            .get("/api/v1/accounts/verify_credentials") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsVerifyCredentialsGetにread_accountsでアクセスできる`() {
        mockMvc
            .get("/api/v1/accounts/verify_credentials") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:accounts")))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsVerifyCredentialsGetに匿名でアクセスすると401() {
        mockMvc
            .get("/api/v1/accounts/verify_credentials")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostに匿名でPOSTしたらアカウントを作成できる() = runTest {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "api-test-user-1")
                param("password", "very-secure-password")
                param("email", "test@example.com")
                param("agreement", "true")
                param("locale", "")
                with(csrf())
            }
            .asyncDispatch()
            .andExpect { status { isFound() } }

        actorRepository.findByNameAndDomain("api-test-user-1", "example.com")
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostで必須パラメーター以外を省略しても作成できる() = runTest {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "api-test-user-2")
                param("password", "very-secure-password")
                with(csrf())
            }
            .asyncDispatch()
            .andExpect { status { isFound() } }

        actorRepository.findByNameAndDomain("api-test-user-2", "example.com")
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostでusernameパラメーターを省略したら400() = runTest {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("password", "api-test-user-3")
                with(csrf())
            }
            .andDo { print() }
            .andExpect { status { isUnprocessableEntity() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostでpasswordパラメーターを省略したら400() = runTest {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "api-test-user-4")
                with(csrf())
            }
            .andExpect { status { isUnprocessableEntity() } }
    }

    @Test
    @Disabled("JSONでも作れるようにするため")
    @WithAnonymousUser
    fun apiV1AccountsPostでJSONで作ろうとしても400() {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"username":"api-test-user-5","password":"very-very-secure-password"}"""
                with(csrf())
            }
            .andExpect { status { isUnsupportedMediaType() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostにCSRFトークンは必要() {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "api-test-user-2")
                param("password", "very-secure-password")
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1AccountsIdGet 匿名でアカウント情報を取得できる`() {
        mockMvc
            .get("/api/v1/accounts/1")
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsIdFollowPost write_follows権限でPOSTでフォローできる`() {
        mockMvc
            .post("/api/v1/accounts/2/follow") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:follows")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsIdFollowPost write権限でPOSTでフォローできる`() {
        mockMvc
            .post("/api/v1/accounts/2/follow") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsIdFollowPost read権限でだと403`() {
        mockMvc
            .post("/api/v1/accounts/2/follow") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1AAccountsIdFollowPost 匿名だと401`() {
        mockMvc
            .post("/api/v1/accounts/2/follow") {
                contentType = MediaType.APPLICATION_JSON
                with(csrf())
            }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1AAccountsIdFollowPost 匿名の場合通常csrfトークンは持ってないので403`() {
        mockMvc
            .post("/api/v1/accounts/2/follow") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV1AccountsRelationshipsGet 匿名だと401`() {
        mockMvc
            .get("/api/v1/accounts/relationships")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `apiV1AccountsRelationshipsGet read_follows権限を持っていたら取得できる`() {
        mockMvc
            .get("/api/v1/accounts/relationships") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:follows")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsRelationshipsGet read権限を持っていたら取得できる`() {
        mockMvc
            .get("/api/v1/accounts/relationships") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsRelationshipsGet write権限だと403`() {
        mockMvc
            .get("/api/v1/accounts/relationships") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write")))
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    @Sql("/sql/accounts/apiV1AccountsIdFollowPost フォローできる.sql")
    fun `apiV1AccountsIdFollowPost フォローできる`() = runTest {
        mockMvc
            .post("/api/v1/accounts/3733363/follow") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "37335363") }.authorities(SimpleGrantedAuthority("SCOPE_write")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }

        val alreadyFollow =
            relationshipRepository.findByActorIdAndTargetId(ActorId(3733363), ActorId(37335363))?.following

        assertThat(alreadyFollow).isTrue()
    }

    @Test
    fun `apiV1AccountsIdMutePost write権限でミュートできる`() {
        mockMvc
            .post("/api/v1/accounts/2/mute") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsIdMutePost write_mutes権限でミュートできる`() {
        mockMvc
            .post("/api/v1/accounts/2/mute") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:mutes")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsIdMutePost read権限だと403`() = runTest {
        mockMvc
            .post("/api/v1/accounts/2/mute") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1AccountsIdMutePost 匿名だと401`() = runTest {
        mockMvc
            .post("/api/v1/accounts/2/mute") {
                contentType = MediaType.APPLICATION_JSON
                with(csrf())
            }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1AccountsIdMutePost csrfトークンがないと403`() = runTest {
        mockMvc
            .post("/api/v1/accounts/2/mute") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV1AccountsIdUnmutePost write権限でアンミュートできる`() {
        mockMvc
            .post("/api/v1/accounts/2/unmute") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsIdUnmutePost write_mutes権限でアンミュートできる`() {
        mockMvc
            .post("/api/v1/accounts/2/unmute") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:mutes")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsIdUnmutePost read権限だと403`() = runTest {
        mockMvc
            .post("/api/v1/accounts/2/unmute") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1AccountsIdUnmutePost 匿名だと401`() = runTest {
        mockMvc
            .post("/api/v1/accounts/2/unmute") {
                contentType = MediaType.APPLICATION_JSON
                with(csrf())
            }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1AccountsIdUnmutePost csrfトークンがないと403`() = runTest {
        mockMvc
            .post("/api/v1/accounts/2/unmute") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV1MutesGet read権限でミュートしているアカウント一覧を取得できる`() {
        mockMvc
            .get("/api/v1/mutes") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1MutesGet read_mutes権限でミュートしているアカウント一覧を取得できる`() {
        mockMvc
            .get("/api/v1/mutes") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:mutes")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1MutesGet write権限だと403`() {
        mockMvc
            .get("/api/v1/mutes") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write")))
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1MutesGet 匿名だと401`() {
        mockMvc
            .get("/api/v1/mutes")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `apiV1AccountsIdStatusesGet read権限で取得できる`() {
        mockMvc
            .get("/api/v1/accounts/1/statuses")
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    @WithAnonymousUser
    fun `apiV1AccountsIdStatusesGet 匿名でもpublic投稿を取得できる`() {
        mockMvc
            .get("/api/v1/accounts/1/statuses")
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
