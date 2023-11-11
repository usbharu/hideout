package activitypub.inbox

import dev.usbharu.hideout.SpringApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import util.TestTransaction
import util.WithHttpSignature

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
class InboxTest {

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
    @WithAnonymousUser
    fun `匿名でinboxにPOSTしたら401`() {
        mockMvc
            .post("/inbox") {
                content = "{}"
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithHttpSignature
    fun 有効なHttpSignatureでPOSTしたら202() {
        mockMvc
            .post("/inbox") {
                content = "{}"
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect { status { isAccepted() } }
    }

    @Test
    @WithAnonymousUser
    fun `匿名でuser-inboxにPOSTしたら401`() {
        mockMvc
            .post("/users/hoge/inbox") {
                content = "{}"
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithHttpSignature
    fun 有効なHttpSignaturesでPOSTしたら202() {
        mockMvc
            .post("/users/hoge/inbox") {
                content = "{}"
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect { status { isAccepted() } }
    }

    @TestConfiguration
    class Configuration {
        @Bean
        fun testTransaction() = TestTransaction
    }
}