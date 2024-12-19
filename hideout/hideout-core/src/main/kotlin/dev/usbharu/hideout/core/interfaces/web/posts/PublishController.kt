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

package dev.usbharu.hideout.core.interfaces.web.posts

import dev.usbharu.hideout.core.application.actor.GetUserDetail
import dev.usbharu.hideout.core.application.actor.GetUserDetailApplicationService
import dev.usbharu.hideout.core.application.instance.GetLocalInstanceApplicationService
import dev.usbharu.hideout.core.application.post.RegisterLocalPost
import dev.usbharu.hideout.core.application.post.RegisterLocalPostApplicationService
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class PublishController(
    private val getLocalInstanceApplicationService: GetLocalInstanceApplicationService,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val getUserDetailApplicationService: GetUserDetailApplicationService,
    private val userRegisterLocalPostApplicationService: RegisterLocalPostApplicationService
) {
    @GetMapping("/publish")
    suspend fun publish(model: Model, @RequestParam("reply_to") replyTo: Long?, @RequestParam repost: Long?): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()

        if (principal.userDetailId == null) {
            throw AccessDeniedException("403 Forbidden")
        }

        val instance = getLocalInstanceApplicationService.execute(Unit, principal)

        @Suppress("UnsafeCallOnNullableType")
        val userDetail = getUserDetailApplicationService.execute(GetUserDetail(principal.userDetailId!!.id), principal)
        model.addAttribute("instance", instance)
        model.addAttribute("user", userDetail)
        model.addAttribute("form", PublishPost(reply_to = replyTo, repost = repost))
        return "post-postForm"
    }

    @PostMapping("/publish")
    suspend fun publishForm(@ModelAttribute publishPost: PublishPost): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()
        if (principal.userDetailId == null) {
            throw AccessDeniedException("403 Forbidden")
        }

        val command = RegisterLocalPost(
            content = publishPost.status.orEmpty(),
            overview = publishPost.overview,
            visibility = Visibility.valueOf(publishPost.visibility.uppercase()),
            repostId = publishPost.repost,
            replyId = publishPost.reply_to,
            sensitive = false,
            mediaIds = emptyList()
        )
        val id = userRegisterLocalPostApplicationService.execute(command, principal)

        return "redirect:/users/${principal.acct?.userpart}/posts/$id"
    }
}
