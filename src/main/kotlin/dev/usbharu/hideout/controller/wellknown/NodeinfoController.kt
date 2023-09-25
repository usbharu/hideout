package dev.usbharu.hideout.controller.wellknown

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.wellknown.Nodeinfo
import dev.usbharu.hideout.domain.model.wellknown.Nodeinfo2_0
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NodeinfoController(private val applicationConfig: ApplicationConfig) {
    @GetMapping("/.well-known/nodeinfo")
    fun nodeinfo(): ResponseEntity<Nodeinfo> {
        return ResponseEntity(
            Nodeinfo(
                listOf(
                    Nodeinfo.Links(
                        "http://nodeinfo.diaspora.software/ns/schema/2.0",
                        "${applicationConfig.url}/nodeinfo/2.0"
                    )
                )
            ),
            HttpStatus.OK
        )
    }

    @GetMapping("/nodeinfo/2.0")
    @Suppress("FunctionNaming")
    fun nodeinfo2_0(): ResponseEntity<Nodeinfo2_0> {
        return ResponseEntity(
            Nodeinfo2_0(
                version = "2.0",
                software = Nodeinfo2_0.Software(
                    name = "hideout",
                    version = "0.0.1"
                ),
                protocols = listOf("activitypub"),
                services = Nodeinfo2_0.Services(
                    inbound = emptyList(),
                    outbound = emptyList()
                ),
                openRegistrations = false,
                usage = Nodeinfo2_0.Usage(
                    users = Nodeinfo2_0.Usage.Users(
                        total = 1,
                        activeHalfYear = 1,
                        activeMonth = 1
                    ),
                    localPosts = 1,
                    localComments = 0
                ),
                metadata = Nodeinfo2_0.Metadata(
                    nodeName = "hideout",
                    nodeDescription = "hideout test server",
                    maintainer = Nodeinfo2_0.Metadata.Maintainer("usbharu", "i@usbharu.dev"),
                    langs = emptyList(),
                    tosUrl = "",
                    repositoryUrl = "https://github.com/usbharu/Hideout",
                    feedbackUrl = "https://github.com/usbharu/Hideout/issues/new/choose",
                )
            ),
            HttpStatus.OK
        )
    }
}
