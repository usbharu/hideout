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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * メディアの保存にローカルファイルシステムを使用する際のコンフィグ
 *
 * @property path フォゾンする場所へのパス。 /から始めると絶対パスとなります。
 * @property publicUrl 公開用URL 省略可能 指定するとHideoutがファイルを配信しなくなります。
 */
@ConfigurationProperties("hideout.storage.local")
@ConditionalOnProperty("hideout.storage.type", havingValue = "local", matchIfMissing = true)
data class LocalStorageConfig(
    val path: String = "files",
    val publicUrl: String?
)
