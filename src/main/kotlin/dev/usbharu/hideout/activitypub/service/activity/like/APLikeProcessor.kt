package dev.usbharu.hideout.activitypub.service.activity.like

import dev.usbharu.hideout.activitypub.domain.exception.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteService
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.service.reaction.ReactionService
import org.springframework.stereotype.Service

@Service
class APLikeProcessor(
    transaction: Transaction,
    private val apUserService: APUserService,
    private val apNoteService: APNoteService,
    private val reactionService: ReactionService
) :
    AbstractActivityPubProcessor<Like>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Like>) {
        val actor = activity.activity.actor
        val content = activity.activity.content

        val target = activity.activity.apObject

        val personWithEntity = apUserService.fetchPersonWithEntity(actor)

        try {
            val post = apNoteService.fetchNoteWithEntity(target).second
            reactionService.receiveReaction(
                content,
                actor.substringAfter("://").substringBefore("/"),
                personWithEntity.second.id,
                post.id
            )

            logger.debug("SUCCESS Add Like($content) from ${personWithEntity.second.url} to ${post.url}")
        } catch (e: FailedToGetActivityPubResourceException) {
            logger.debug("FAILED failed to get {}", target)
            logger.trace("", e)
            return
        }
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Like

    override fun type(): Class<Like> = Like::class.java
}
