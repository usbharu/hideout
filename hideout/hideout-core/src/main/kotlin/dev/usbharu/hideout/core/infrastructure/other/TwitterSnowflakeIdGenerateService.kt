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

package dev.usbharu.hideout.core.infrastructure.other

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

// 2010-11-04T01:42:54.657
@Suppress("MagicNumber")
@Service
@Primary
object TwitterSnowflakeIdGenerateService : SnowflakeIdGenerateService(1288834974657L)
