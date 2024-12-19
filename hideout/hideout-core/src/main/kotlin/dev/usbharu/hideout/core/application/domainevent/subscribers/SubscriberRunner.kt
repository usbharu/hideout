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

package dev.usbharu.hideout.core.application.domainevent.subscribers

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SubscriberRunner(
    private val subscribers: List<Subscriber>,
    private val domainEventSubscriber: DomainEventSubscriber
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        subscribers.forEach { it.init() }

        if (logger.isDebugEnabled) {
            val stringListMap = domainEventSubscriber.getSubscribers()

            val header = """
                |====== Domain Event Subscribers Report =====
                |
                |
                |
            """.trimMargin()

            val value = stringListMap.map {
                it.key + "\n\t" + it.value.joinToString("\n", "[", "]") { suspendFunction1 ->
                    suspendFunction1::class.qualifiedName.orEmpty()
                }
            }.joinToString("\n\n\n")

            val footer = """
                |
                |
                |
                |===== Domain Event Subscribers Report =====
            """.trimMargin()

            logger.debug("{}{}{}", header, value, footer)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SubscriberRunner::class.java)
    }
}
