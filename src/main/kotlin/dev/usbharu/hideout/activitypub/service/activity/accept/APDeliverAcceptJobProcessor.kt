package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

@Service
class APDeliverAcceptJobProcessor(
    private val apRequestService: APRequestService,
    private val userQueryService: UserQueryService,
    private val deliverAcceptJob: DeliverAcceptJob
) :
    JobProcessor<DeliverAcceptJobParam, DeliverAcceptJob> {
    override suspend fun process(param: DeliverAcceptJobParam) {
        apRequestService.apPost(param.inbox, param.accept, userQueryService.findById(param.signer))
    }

    override fun job(): DeliverAcceptJob = deliverAcceptJob
}
