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

package dev.usbharu.hideout.core.interfaces.web.user

import dev.usbharu.hideout.core.application.actor.GetActorDetail
import dev.usbharu.hideout.core.application.actor.GetActorDetailApplicationService
import dev.usbharu.hideout.core.application.instance.GetLocalInstanceApplicationService
import dev.usbharu.hideout.core.application.relationship.*
import dev.usbharu.hideout.core.application.timeline.GetUserTimeline
import dev.usbharu.hideout.core.application.timeline.GetUserTimelineApplicationService
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class UserController(
    private val getLocalInstanceApplicationService: GetLocalInstanceApplicationService,
    private val getUserDetailApplicationService: GetActorDetailApplicationService,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val getUserTimelineApplicationService: GetUserTimelineApplicationService,
    private val userFollowRequestApplicationService: UserFollowRequestApplicationService,
    private val getActorDetailApplicationService: GetActorDetailApplicationService,
    private val userUnfollowApplicationService: UserUnfollowApplicationService,
    private val userGetRelationshipApplicationService: GetRelationshipApplicationService
) {
    @GetMapping("/users/{name}")
    suspend fun userById(
        @PathVariable name: String,
        @RequestParam("min_id") minId: Long?,
        @RequestParam("max_id") maxId: Long?,
        @RequestParam("since_id") sinceId: Long?,
        model: Model
    ): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()

        model.addAttribute("instance", getLocalInstanceApplicationService.execute(Unit, Anonymous))
        val actorDetail = getUserDetailApplicationService.execute(GetActorDetail(Acct.of(name)), principal)
        model.addAttribute(
            "user",
            actorDetail
        )
        val relationship =
            if (principal is LocalUser) {
                userGetRelationshipApplicationService.execute(GetRelationship(actorDetail.id), principal)
            } else {
                null
            }

        model.addAttribute("relationship", relationship)
        model.addAttribute(
            "userTimeline",
            getUserTimelineApplicationService.execute(
                GetUserTimeline(
                    actorDetail.id,
                    Page.of(maxId, sinceId, minId, 20)
                ),
                principal
            )
        )
        return "userById"
    }

    @PostMapping("/users/{name}/follow")
    suspend fun follow(@PathVariable name: String): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()

        val actorDetail = getActorDetailApplicationService.execute(GetActorDetail(Acct.of(name), null), principal)
        userFollowRequestApplicationService.execute(FollowRequest((actorDetail.id)), principal)

        return "redirect:/users/$name"
    }

    @PostMapping("/users/{name}/unfollow")
    suspend fun unfollow(@PathVariable name: String): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()

        val actorDetail = getActorDetailApplicationService.execute(GetActorDetail(Acct.of(name), null), principal)
        userUnfollowApplicationService.execute(Unfollow((actorDetail.id)), principal)

        return "redirect:/users/$name"
    }
}
