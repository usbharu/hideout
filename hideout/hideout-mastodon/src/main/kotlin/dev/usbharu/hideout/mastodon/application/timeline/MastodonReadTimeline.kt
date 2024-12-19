package dev.usbharu.hideout.mastodon.application.timeline

import dev.usbharu.hideout.core.domain.model.support.page.Page

class MastodonReadTimeline(
    val timelineId: Long,
    val mediaOnly: Boolean,
    val localOnly: Boolean,
    val remoteOnly: Boolean,
    val page: Page
)
