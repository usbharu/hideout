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

package dev.usbharu.hideout.generate

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.validation.BindException
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor

@Suppress("TooGenericExceptionCaught")
class JsonOrFormModelMethodProcessor(
    private val modelAttributeMethodProcessor: ModelAttributeMethodProcessor,
    private val requestResponseBodyMethodProcessor: RequestResponseBodyMethodProcessor,
) : HandlerMethodArgumentResolver {
    private val isJsonRegex = Regex("application/((\\w*)\\+)?json")

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(JsonOrFormBind::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val contentType = webRequest.getHeader("Content-Type").orEmpty()
        logger.trace("ContentType is {}", contentType)
        if (contentType.contains(isJsonRegex)) {
            logger.trace("Determine content type as json.")
            return requestResponseBodyMethodProcessor.resolveArgument(
                parameter,
                mavContainer,
                webRequest,
                binderFactory
            )
        }

        return try {
            modelAttributeMethodProcessor.resolveArgument(parameter, mavContainer, webRequest, binderFactory)
        } catch (e: BindException) {
            throw e
        } catch (exception: Exception) {
            try {
                requestResponseBodyMethodProcessor.resolveArgument(parameter, mavContainer, webRequest, binderFactory)
            } catch (e: BindException) {
                throw e
            } catch (e: Exception) {
                logger.warn("Failed to bind request (1)", exception)
                logger.warn("Failed to bind request (2)", e)
                throw IllegalArgumentException("Failed to bind request.")
            }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(JsonOrFormModelMethodProcessor::class.java)
    }
}
