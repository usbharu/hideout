package dev.usbharu.hideout.mastodon.infrastructure.exposedquery

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import kotlinx.coroutines.test.runTest
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional


@Sql("/sql/actors.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql("/sql/relationships.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@SpringBootTest(classes = [SpringApplication::class])
class StatusQueryServiceImplTest {

    @Autowired
    lateinit var statusQueryServiceImpl: StatusQueryServiceImpl

    @Test
    fun フォロワー限定をフォロワー以外は見れない() = runTest {
        val status =
            statusQueryServiceImpl.findByPostId(4, LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com")))

        assertNull(status)
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