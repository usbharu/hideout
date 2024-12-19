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

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.instance.GetLocalInstanceApplicationService
import dev.usbharu.hideout.core.application.post.GetPostDetail
import dev.usbharu.hideout.core.application.post.GetPostDetailApplicationService
import dev.usbharu.hideout.core.application.reaction.CreateReaction
import dev.usbharu.hideout.core.application.reaction.RemoveReaction
import dev.usbharu.hideout.core.application.reaction.UserCreateReactionApplicationService
import dev.usbharu.hideout.core.application.reaction.UserRemoveReactionApplicationService
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class PostsController(
    private val getPostDetailApplicationService: GetPostDetailApplicationService,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val getLocalInstanceApplicationService: GetLocalInstanceApplicationService,
    private val userCreateReactionApplicationService: UserCreateReactionApplicationService,
    private val userRemoveReactionApplicationService: UserRemoveReactionApplicationService
) {
    @GetMapping("/users/{name}/posts/{id}")
    suspend fun postById(@PathVariable id: Long, model: Model): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()
        try {
            val post = getPostDetailApplicationService.execute(GetPostDetail(id), principal)
            val instance = getLocalInstanceApplicationService.execute(Unit, principal)
            model.addAttribute("post", post)
            model.addAttribute("instance", instance)
        } catch (e: PermissionDeniedException) {
            throw AccessDeniedException("403 Forbidden", e)
        }

        return "postById"
    }

    @PostMapping("/users/{name}/posts/{id}/favourite")
    suspend fun favourite(@PathVariable id: Long, @PathVariable name: String): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()
        userCreateReactionApplicationService.execute(
            CreateReaction(
                id,
                null,
                "❤"
            ),
            principal
        )
        return "redirect:/users/$name/posts/$id"
    }

    @PostMapping("/users/{name}/posts/{id}/unfavourite")
    suspend fun unfavourite(@PathVariable id: Long, @PathVariable name: String): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()
        userRemoveReactionApplicationService.execute(
            RemoveReaction(
                id,
                null,
                "❤"
            ),
            principal
        )
        return "redirect:/users/$name/posts/$id"
    }
}
