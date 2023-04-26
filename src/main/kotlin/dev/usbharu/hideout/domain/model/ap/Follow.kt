package dev.usbharu.hideout.domain.model.ap

open class Follow : Object {
    var `object`: String? = null
    var actor: String? = null

    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        `object`: String?,
        actor: String?
    ) : super(add(type, "Follow"), name) {
        this.`object` = `object`
        this.actor = actor
    }


}
