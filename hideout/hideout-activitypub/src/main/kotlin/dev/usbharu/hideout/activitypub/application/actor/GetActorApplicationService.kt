package dev.usbharu.hideout.activitypub.application.actor

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetActorApplicationService(transaction: Transaction) : AbstractApplicationService<String, Actor>(
    transaction,
    logger
) {
    override suspend fun internalExecute(command: String, principal: Principal): Actor {
        TODO()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetActorApplicationService::class.java)
    }
}