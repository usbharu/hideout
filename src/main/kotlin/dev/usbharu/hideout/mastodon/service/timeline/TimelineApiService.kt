package dev.usbharu.hideout.mastodon.service.timeline

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.service.timeline.GenerateTimelineService
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import org.springframework.stereotype.Service

@Suppress("LongParameterList")
interface TimelineApiService {
    suspend fun publicTimeline(
        localOnly: Boolean = false,
        remoteOnly: Boolean = false,
        mediaOnly: Boolean = false,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int = 20
    ): List<Status>

    suspend fun homeTimeline(
        userId: Long,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int = 20
    ): List<Status>
}

@Service
class TimelineApiServiceImpl(
    private val generateTimelineService: GenerateTimelineService,
    private val transaction: Transaction
) : TimelineApiService {
    override suspend fun publicTimeline(
        localOnly: Boolean,
        remoteOnly: Boolean,
        mediaOnly: Boolean,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int
    ): List<Status> = transaction.transaction {
        generateTimelineService.getTimeline(
            forUserId = 0,
            localOnly = localOnly,
            mediaOnly = mediaOnly,
            maxId = maxId,
            minId = minId,
            sinceId = sinceId,
            limit = limit
        )
    }

    override suspend fun homeTimeline(
        userId: Long,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int
    ): List<Status> = transaction.transaction {
        generateTimelineService.getTimeline(
            forUserId = userId,
            maxId = maxId,
            minId = minId,
            sinceId = sinceId,
            limit = limit
        )
    }
}