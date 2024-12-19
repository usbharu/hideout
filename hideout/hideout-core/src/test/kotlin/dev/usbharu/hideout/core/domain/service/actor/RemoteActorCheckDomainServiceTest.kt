package dev.usbharu.hideout.core.domain.service.actor

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorPublicKey
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoteActorCheckDomainServiceTest {
    @Test
    fun リモートのドメインならtrueを返す() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        val remoteActor = RemoteActorCheckDomainService(
            ApplicationConfig(
                URI.create("https://local.example.com")
            )
        ).isRemoteActor(
            actor
        )

        assertTrue(remoteActor)
    }

    @Test
    fun ローカルのActorならfalseを返す() {
        val actor = TestActorFactory.create(domain = "local.example.com", publicKey = ActorPublicKey(""))

        val localActor = RemoteActorCheckDomainService(
            ApplicationConfig(
                URI.create("https://local.example.com")
            )
        ).isRemoteActor(
            actor
        )

        assertFalse(localActor)
    }
}