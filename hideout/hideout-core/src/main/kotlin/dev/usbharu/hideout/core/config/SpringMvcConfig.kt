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

import ch.qos.logback.classic.helpers.MDCInsertingServletFilter
import dev.usbharu.hideout.core.infrastructure.springframework.ApplicationRequestLogInterceptor
import dev.usbharu.hideout.core.infrastructure.springframework.SPAInterceptor
import dev.usbharu.hideout.generate.JsonOrFormModelMethodProcessor
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor

@Configuration
class MvcConfigurer(
    private val jsonOrFormModelMethodProcessor: JsonOrFormModelMethodProcessor,
    private val spaInterceptor: SPAInterceptor,
    private val applicationRequestLogInterceptor: ApplicationRequestLogInterceptor
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(jsonOrFormModelMethodProcessor)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(spaInterceptor)
        registry.addInterceptor(applicationRequestLogInterceptor)
    }

    @Bean
    fun mdcFilter(): FilterRegistrationBean<MDCInsertingServletFilter> {
        val bean = FilterRegistrationBean<MDCInsertingServletFilter>()
        bean.filter = MDCInsertingServletFilter()
        bean.addUrlPatterns("/*")
        bean.order = Int.MIN_VALUE
        return bean
    }
}

@Configuration
class JsonOrFormModelMethodProcessorConfig {
    @Bean
    fun jsonOrFormModelMethodProcessor(converter: List<HttpMessageConverter<*>>): JsonOrFormModelMethodProcessor {
        return JsonOrFormModelMethodProcessor(
            ServletModelAttributeMethodProcessor(true),
            RequestResponseBodyMethodProcessor(
                converter
            )
        )
    }
}
