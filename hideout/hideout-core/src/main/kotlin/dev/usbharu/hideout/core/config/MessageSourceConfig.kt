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

package dev.usbharu.hideout.core.config

import org.springframework.boot.autoconfigure.context.MessageSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.ReloadableResourceBundleMessageSource

@Configuration
@Profile("dev")
class MessageSourceConfig {
    @Bean
    fun messageSource(messageSourceProperties: MessageSourceProperties): MessageSource {
        val reloadableResourceBundleMessageSource = ReloadableResourceBundleMessageSource()
        reloadableResourceBundleMessageSource.setBasename("classpath:" + messageSourceProperties.basename)
        reloadableResourceBundleMessageSource.setCacheSeconds(0)
        return reloadableResourceBundleMessageSource
    }

    @Bean
    @Profile("dev")
    @ConfigurationProperties(prefix = "spring.messages")
    fun messageSourceProperties(): MessageSourceProperties = MessageSourceProperties()
}
