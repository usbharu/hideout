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

package dev.usbharu.hideout.core.domain.model.support.timelineobjectdetail

import dev.usbharu.hideout.core.application.model.Reactions
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectWarnFilter
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import java.time.Instant

data class TimelineObjectDetail(
    val id: TimelineObjectId,
    val postId: PostId,
    val timelineUserDetail: UserDetail,
    val post: Post,
    val postMedias: List<Media>,
    val postActor: Actor,
    val postActorIconMedia: Media?,
    val replyPost: Post?,
    val replyPostMedias: List<Media>?,
    val replyPostActor: Actor?,
    val replyPostActorIconMedia: Media?,
    val repostPost: Post?,
    val repostPostMedias: List<Media>?,
    val repostPostActor: Actor?,
    val repostPostActorIconMedia: Media?,
    val isPureRepost: Boolean,
    val lastUpdateAt: Instant,
    val hasMediaInRepost: Boolean,
    val warnFilter: List<TimelineObjectWarnFilter>,
    val reactionsList: List<Reactions>,
    val favourited: Boolean
) {
    companion object {
        @Suppress("LongParameterList")
        fun of(
            timelineObject: TimelineObject,
            timelineUserDetail: UserDetail,
            post: Post,
            postMedias: List<Media>,
            postActor: Actor,
            postActorIconMedia: Media?,
            replyPost: Post?,
            replyPostMedias: List<Media>?,
            replyPostActor: Actor?,
            replyPostActorIconMedia: Media?,
            repostPost: Post?,
            repostPostMedias: List<Media>?,
            repostPostActor: Actor?,
            repostPostActorIconMedia: Media?,
            warnFilter: List<TimelineObjectWarnFilter>,
            reactionsList: List<Reactions>,
            favourited: Boolean
        ): TimelineObjectDetail {
            return TimelineObjectDetail(
                id = timelineObject.id,
                postId = post.id,
                timelineUserDetail = timelineUserDetail,
                post = post,
                postMedias = postMedias,
                postActor = postActor,
                postActorIconMedia = postActorIconMedia,
                replyPost = replyPost,
                replyPostMedias = replyPostMedias,
                replyPostActor = replyPostActor,
                replyPostActorIconMedia = replyPostActorIconMedia,
                repostPost = repostPost,
                repostPostMedias = repostPostMedias,
                repostPostActor = repostPostActor,
                repostPostActorIconMedia = repostPostActorIconMedia,
                isPureRepost = timelineObject.isPureRepost,
                lastUpdateAt = timelineObject.lastUpdatedAt,
                hasMediaInRepost = timelineObject.hasMediaInRepost,
                warnFilter = warnFilter,
                reactionsList = reactionsList,
                favourited = favourited
            )
        }
    }
}
