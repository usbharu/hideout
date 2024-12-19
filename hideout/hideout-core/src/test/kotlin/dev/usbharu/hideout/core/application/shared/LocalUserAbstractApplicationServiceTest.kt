package dev.usbharu.hideout.core.application.shared

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import utils.TestTransaction

class LocalUserAbstractApplicationServiceTest {
    @Test
    fun requireFromAPI() = runTest {
        val logger = LoggerFactory.getLogger(javaClass)
        val value = object : LocalUserAbstractApplicationService<Unit, Unit>(TestTransaction, logger) {
            override suspend fun internalExecute(command: Unit, principal: LocalUser) {

            }
        }

        org.junit.jupiter.api.assertThrows<PermissionDeniedException> {
            value.execute(Unit, Anonymous)
        }
    }
}