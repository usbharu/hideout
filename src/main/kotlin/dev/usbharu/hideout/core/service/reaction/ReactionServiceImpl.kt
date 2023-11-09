package dev.usbharu.hideout.core.service.reaction

import dev.usbharu.hideout.activitypub.service.activity.like.APReactionService
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.query.ReactionQueryService
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReactionServiceImpl(
    private val reactionRepository: ReactionRepository,
    private val apReactionService: APReactionService,
    private val reactionQueryService: ReactionQueryService
) : ReactionService {
    override suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long) {
        if (reactionQueryService.reactionAlreadyExist(postId, userId, 0).not()) {
            try {
                reactionRepository.save(
                    Reaction(reactionRepository.generateId(), 0, postId, userId)
                )
            } catch (_: ExposedSQLException) {
            }
        }
    }

    override suspend fun sendReaction(name: String, userId: Long, postId: Long) {
        try {
            val findByPostIdAndUserIdAndEmojiId =
                reactionQueryService.findByPostIdAndUserIdAndEmojiId(postId, userId, 0)
            apReactionService.removeReaction(findByPostIdAndUserIdAndEmojiId)
            reactionRepository.delete(findByPostIdAndUserIdAndEmojiId)
        } catch (_: FailedToGetResourcesException) {
        }
        val reaction = Reaction(reactionRepository.generateId(), 0, postId, userId)
        reactionRepository.save(reaction)
        apReactionService.reaction(reaction)
    }

    override suspend fun removeReaction(userId: Long, postId: Long) {
        try {
            val findByPostIdAndUserIdAndEmojiId =
                reactionQueryService.findByPostIdAndUserIdAndEmojiId(postId, userId, 0)
            reactionRepository.delete(findByPostIdAndUserIdAndEmojiId)
            apReactionService.removeReaction(findByPostIdAndUserIdAndEmojiId)
        } catch (e: FailedToGetResourcesException) {
        }
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(ReactionServiceImpl::class.java)
    }
}
