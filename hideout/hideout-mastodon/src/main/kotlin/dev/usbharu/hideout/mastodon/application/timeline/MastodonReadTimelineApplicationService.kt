package dev.usbharu.hideout.mastodon.application.timeline

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.post.Visibility.*
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.external.timeline.ReadTimelineOption
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Account
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.MediaAttachment
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MastodonReadTimelineApplicationService(
    transaction: Transaction,
    private val timelineRepository: TimelineRepository,
    private val timelineStore: TimelineStore
) :
    AbstractApplicationService<MastodonReadTimeline, PaginationList<Status, Long>>(transaction, logger) {
    override suspend fun internalExecute(
        command: MastodonReadTimeline,
        principal: Principal
    ): PaginationList<Status, Long> {
        val timeline = timelineRepository.findById(TimelineId(command.timelineId))
            ?: throw IllegalArgumentException("Timeline ${command.timelineId} not found.")

        val readTimelineOption = ReadTimelineOption(
            command.mediaOnly,
            command.localOnly,
            command.remoteOnly
        )

        val readTimeline = timelineStore.readTimeline(timeline, readTimelineOption, command.page, principal)

        return PaginationList(
            readTimeline.map {
                Status(
                    it.postId.id.toString(),
                    it.post.url.toString(),
                    it.post.createdAt.toString(),
                    account = Account(
                        id = it.postActor.id.id.toString(),
                        username = it.postActor.name.name,
                        acct = Acct(it.postActor.name.name, it.postActor.domain.domain).toString(),
                        url = it.postActor.url.toString(),
                        displayName = it.postActor.screenName.screenName,
                        note = it.postActor.description.description,
                        avatar = it.postActorIconMedia?.url.toString(),
                        avatarStatic = it.postActorIconMedia?.thumbnailUrl.toString(),
                        header = "",
                        headerStatic = "",
                        locked = false,
                        fields = emptyList(),
                        emojis = emptyList(),
                        bot = false,
                        group = false,
                        discoverable = true,
                        createdAt = it.postActor.createdAt.toString(),
                        statusesCount = it.postActor.postsCount.postsCount,
                        noindex = true,
                        moved = it.postActor.moveTo != null,
                        suspended = it.postActor.suspend,
                        limited = false,
                        lastStatusAt = it.postActor.lastPostAt?.toString(),
                        followersCount = it.postActor.followersCount?.relationshipCount,
                        followingCount = it.postActor.followingCount?.relationshipCount,
                    ),
                    content = it.post.content.content,
                    visibility = when (it.post.visibility) {
                        PUBLIC -> Status.Visibility.public
                        UNLISTED -> Status.Visibility.unlisted
                        FOLLOWERS -> Status.Visibility.private
                        DIRECT -> Status.Visibility.direct
                    },
                    sensitive = it.post.sensitive,
                    spoilerText = it.post.overview?.overview.orEmpty(),
                    mediaAttachments = it.postMedias.map { MediaAttachment(it.id.id.toString()) },
                    mentions = emptyList(),
                    tags = emptyList(),
                    emojis = emptyList(),
                    reblogsCount = 0,
                    favouritesCount = it.reactionsList.sumOf { it.count },
                    repliesCount = 0,
                    url = it.post.url.toString(),
                    text = it.post.content.text,
                    application = null,
                    inReplyToId = it.replyPost?.id?.toString(),
                    inReplyToAccountId = it.replyPostActor?.id?.toString(),
                    reblog = null,
                    poll = null,
                    card = null,
                    language = null,
                    editedAt = null,
                    favourited = it.favourited,
                    reblogged = false,
                    muted = false,
                    bookmarked = false,
                    pinned = false,
                    filtered = emptyList(),
                )
            },
            readTimeline.next?.id,
            readTimeline.prev?.id
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MastodonReadTimelineApplicationService::class.java)
    }
}
