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

package util
//
//import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureUser
//import dev.usbharu.httpsignature.common.HttpHeaders
//import dev.usbharu.httpsignature.common.HttpMethod
//import dev.usbharu.httpsignature.common.HttpRequest
//import org.springframework.security.core.context.SecurityContext
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.security.test.context.support.WithSecurityContextFactory
//import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
//import java.net.URL
//
//class WithMockHttpSignatureSecurityContextFactory :
//    WithSecurityContextFactory<WithMockHttpSignature> {
//
//    private val securityContextStrategy = SecurityContextHolder.getContextHolderStrategy()
//
//    override fun createSecurityContext(annotation: WithMockHttpSignature): SecurityContext {
//        val preAuthenticatedAuthenticationToken = PreAuthenticatedAuthenticationToken(
//            annotation.keyId, HttpRequest(
//                URL(annotation.url),
//                HttpHeaders(mapOf()), HttpMethod.valueOf(annotation.method.uppercase())
//            )
//        )
//        val httpSignatureUser = HttpSignatureUser(
//            annotation.username,
//            annotation.domain,
//            annotation.id,
//            true,
//            true,
//            mutableListOf()
//        )
//        preAuthenticatedAuthenticationToken.details = httpSignatureUser
//        preAuthenticatedAuthenticationToken.isAuthenticated = true
//        val emptyContext = securityContextStrategy.createEmptyContext()
//        emptyContext.authentication = preAuthenticatedAuthenticationToken
//        return emptyContext
//    }
//}
