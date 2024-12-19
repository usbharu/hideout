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

import org.bytedeco.ffmpeg.global.avcodec
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("hideout.media.video.ffmpeg")
data class FFmpegVideoConfig(
    val frameRate: Int = 60,
    val maxWidth: Int = 1920,
    val maxHeight: Int = 1080,
    val format: String = "mp4",
    val videoCodec: Int = avcodec.AV_CODEC_ID_H264,
    val audioCodec: Int = avcodec.AV_CODEC_ID_AAC,
    val videoQuality: Double = 1.0,
    val videoOption: List<String> = listOf("preset", "ultrafast"),
    val maxBitrate: Int = 1300000,
)
