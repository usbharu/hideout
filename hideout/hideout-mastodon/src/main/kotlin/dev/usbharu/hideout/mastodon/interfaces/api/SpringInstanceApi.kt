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

package dev.usbharu.hideout.mastodon.interfaces.api

import dev.usbharu.hideout.mastodon.interfaces.api.generated.InstanceApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.ExtendedDescription
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Instance
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.V1Instance
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SpringInstanceApi : InstanceApi {

    override suspend fun apiV1InstanceExtendedDescriptionGet(): ResponseEntity<ExtendedDescription> =
        super.apiV1InstanceExtendedDescriptionGet()

    override suspend fun apiV1InstanceGet(): ResponseEntity<V1Instance> = super.apiV1InstanceGet()

    override suspend fun apiV2InstanceGet(): ResponseEntity<Instance> = super.apiV2InstanceGet()
}
