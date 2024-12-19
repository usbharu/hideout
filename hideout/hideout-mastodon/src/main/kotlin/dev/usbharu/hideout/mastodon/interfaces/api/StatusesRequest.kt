package dev.usbharu.hideout.mastodon.interfaces.api

import com.fasterxml.jackson.annotation.JsonProperty
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.mastodon.interfaces.api.StatusesRequest.Visibility.*
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.StatusesRequestPoll

@Suppress("VariableNaming", "EnumEntryName")
class StatusesRequest {
    @JsonProperty("status")
    var status: String? = null

    @JsonProperty("media_ids")
    var media_ids: List<String> = emptyList()

    @JsonProperty("poll")
    var poll: StatusesRequestPoll? = null

    @JsonProperty("in_reply_to_id")
    var in_reply_to_id: String? = null

    @JsonProperty("sensitive")
    var sensitive: Boolean? = null

    @JsonProperty("spoiler_text")
    var spoiler_text: String? = null

    @JsonProperty("visibility")
    var visibility: Visibility? = null

    @JsonProperty("language")
    var language: String? = null

    @JsonProperty("scheduled_at")
    var scheduled_at: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StatusesRequest) return false

        if (status != other.status) return false
        if (media_ids != other.media_ids) return false
        if (poll != other.poll) return false
        if (in_reply_to_id != other.in_reply_to_id) return false
        if (sensitive != other.sensitive) return false
        if (spoiler_text != other.spoiler_text) return false
        if (visibility != other.visibility) return false
        if (language != other.language) return false
        if (scheduled_at != other.scheduled_at) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status?.hashCode() ?: 0
        result = 31 * result + media_ids.hashCode()
        result = 31 * result + (poll?.hashCode() ?: 0)
        result = 31 * result + (in_reply_to_id?.hashCode() ?: 0)
        result = 31 * result + (sensitive?.hashCode() ?: 0)
        result = 31 * result + (spoiler_text?.hashCode() ?: 0)
        result = 31 * result + (visibility?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (scheduled_at?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "StatusesRequest(status=$status, mediaIds=$media_ids, poll=$poll, inReplyToId=$in_reply_to_id, " +
                "sensitive=$sensitive, spoilerText=$spoiler_text, visibility=$visibility, language=$language," +
                " scheduledAt=$scheduled_at)"
    }

    @Suppress("EnumNaming", "EnumEntryNameCase")
    enum class Visibility {
        `public`,
        unlisted,
        private,
        direct
    }
}

fun StatusesRequest.Visibility?.toPostVisibility(): Visibility {
    return when (this) {
        public -> Visibility.PUBLIC
        unlisted -> Visibility.UNLISTED
        private -> Visibility.FOLLOWERS
        direct -> Visibility.DIRECT
        null -> Visibility.PUBLIC
    }
}

fun StatusesRequest.Visibility?.toStatusVisibility(): Status.Visibility {
    return when (this) {
        public -> Status.Visibility.public
        unlisted -> Status.Visibility.unlisted
        private -> Status.Visibility.private
        direct -> Status.Visibility.direct
        null -> Status.Visibility.public
    }
}
