package dev.usbharu.hideout.domain.model.hideout.entity

data class Post(
    val id:Long,
    val userId: Long,
    val overview:String? = null,
    val text:String,
    val createdAt:Long,
    val visibility: Int,
    val url:String,
    val repostId:Long? = null,
    val replyId:Long? = null
)
