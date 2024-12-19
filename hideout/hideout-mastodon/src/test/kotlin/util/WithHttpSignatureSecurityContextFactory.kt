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

//class WithHttpSignatureSecurityContextFactory(
//    private val actorRepository: ActorRepository,
//    private val transaction: Transaction
//) : WithSecurityContextFactory<WithHttpSignature> {
//
//    private val securityContextStrategy = SecurityContextHolder.getContextHolderStrategy()
//
//    override fun createSecurityContext(annotation: WithHttpSignature): SecurityContext = runBlocking {
//        val preAuthenticatedAuthenticationToken = PreAuthenticatedAuthenticationToken(
//            annotation.keyId, HttpRequest(
//                URL("https://example.com/inbox"),
//                HttpHeaders(mapOf()), HttpMethod.GET
//            )
//        )
//        val httpSignatureUser = transaction.transaction {
//            val findByKeyId =
//                actorRepository.findByKeyId(annotation.keyId) ?: throw IllegalArgumentException(annotation.keyId)
//            HttpSignatureUser(
//                findByKeyId.name,
//                findByKeyId.domain,
//                findByKeyId.id,
//                true,
//                true,
//                mutableListOf()
//            )
//        }
//        preAuthenticatedAuthenticationToken.details = httpSignatureUser
//        preAuthenticatedAuthenticationToken.isAuthenticated = true
//        val emptyContext = securityContextStrategy.createEmptyContext()
//        emptyContext.authentication = preAuthenticatedAuthenticationToken
//        return@runBlocking emptyContext
//    }
//
//}
