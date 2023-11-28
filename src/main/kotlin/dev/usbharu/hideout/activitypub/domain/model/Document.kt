package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Document(
    type: List<String> = emptyList(),
    override val name: String = "",
    mediaType: String,
    url: String
) : Object(
    type = add(type, "Document")
),
    HasName {

    var mediaType: String? = mediaType
    var url: String? = url

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Document

        if (mediaType != other.mediaType) return false
        if (url != other.url) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String = "Document(mediaType=$mediaType, url=$url, name='$name') ${super.toString()}"
}
