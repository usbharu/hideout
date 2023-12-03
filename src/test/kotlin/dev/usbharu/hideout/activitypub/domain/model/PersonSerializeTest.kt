package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.junit.jupiter.api.Test

class PersonSerializeTest {
    @Test
    fun MastodonのPersonのデシリアライズができる() {
        val personString = """
            {
              "@context": [
                "https://www.w3.org/ns/activitystreams",
                "https://w3id.org/security/v1"
              ],
              "id": "https://mastodon.social/users/Gargron",
              "type": "Person",
              "following": "https://mastodon.social/users/Gargron/following",
              "followers": "https://mastodon.social/users/Gargron/followers",
              "inbox": "https://mastodon.social/users/Gargron/inbox",
              "outbox": "https://mastodon.social/users/Gargron/outbox",
              "featured": "https://mastodon.social/users/Gargron/collections/featured",
              "featuredTags": "https://mastodon.social/users/Gargron/collections/tags",
              "preferredUsername": "Gargron",
              "name": "Eugen Rochko",
              "summary": "\u003cp\u003eFounder, CEO and lead developer \u003cspan class=\"h-card\"\u003e\u003ca href=\"https://mastodon.social/@Mastodon\" class=\"u-url mention\"\u003e@\u003cspan\u003eMastodon\u003c/span\u003e\u003c/a\u003e\u003c/span\u003e, Germany.\u003c/p\u003e",
              "url": "https://mastodon.social/@Gargron",
              "manuallyApprovesFollowers": false,
              "discoverable": true,
              "published": "2016-03-16T00:00:00Z",
              "devices": "https://mastodon.social/users/Gargron/collections/devices",
              "alsoKnownAs": [
                "https://tooting.ai/users/Gargron"
              ],
              "publicKey": {
                "id": "https://mastodon.social/users/Gargron#main-key",
                "owner": "https://mastodon.social/users/Gargron",
                "publicKeyPem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvXc4vkECU2/CeuSo1wtn\nFoim94Ne1jBMYxTZ9wm2YTdJq1oiZKif06I2fOqDzY/4q/S9uccrE9Bkajv1dnkO\nVm31QjWlhVpSKynVxEWjVBO5Ienue8gND0xvHIuXf87o61poqjEoepvsQFElA5ym\novljWGSA/jpj7ozygUZhCXtaS2W5AD5tnBQUpcO0lhItYPYTjnmzcc4y2NbJV8hz\n2s2G8qKv8fyimE23gY1XrPJg+cRF+g4PqFXujjlJ7MihD9oqtLGxbu7o1cifTn3x\nBfIdPythWu5b4cujNsB3m3awJjVmx+MHQ9SugkSIYXV0Ina77cTNS0M2PYiH1PFR\nTwIDAQAB\n-----END PUBLIC KEY-----\n"
              },
              "tag": [],
              "attachment": [
                {
                  "type": "PropertyValue",
                  "name": "Patreon",
                  "value": "\u003ca href=\"https://www.patreon.com/mastodon\" target=\"_blank\" rel=\"nofollow noopener noreferrer me\"\u003e\u003cspan class=\"invisible\"\u003ehttps://www.\u003c/span\u003e\u003cspan class=\"\"\u003epatreon.com/mastodon\u003c/span\u003e\u003cspan class=\"invisible\"\u003e\u003c/span\u003e\u003c/a\u003e"
                },
                {
                  "type": "PropertyValue",
                  "name": "GitHub",
                  "value": "\u003ca href=\"https://github.com/Gargron\" target=\"_blank\" rel=\"nofollow noopener noreferrer me\"\u003e\u003cspan class=\"invisible\"\u003ehttps://\u003c/span\u003e\u003cspan class=\"\"\u003egithub.com/Gargron\u003c/span\u003e\u003cspan class=\"invisible\"\u003e\u003c/span\u003e\u003c/a\u003e"
                }
              ],
              "endpoints": {
                "sharedInbox": "https://mastodon.social/inbox"
              },
              "icon": {
                "type": "Image",
                "mediaType": "image/jpeg",
                "url": "https://files.mastodon.social/accounts/avatars/000/000/001/original/dc4286ceb8fab734.jpg"
              },
              "image": {
                "type": "Image",
                "mediaType": "image/jpeg",
                "url": "https://files.mastodon.social/accounts/headers/000/000/001/original/3b91c9965d00888b.jpeg"
              }
            }

        """.trimIndent()


        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Person>(personString)
    }
}
