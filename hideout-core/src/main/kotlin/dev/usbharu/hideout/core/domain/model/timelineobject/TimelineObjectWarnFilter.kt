package dev.usbharu.hideout.core.domain.model.timelineobject

import dev.usbharu.hideout.core.domain.model.filter.FilterId

data class TimelineObjectWarnFilter(val filterId: FilterId, val matchedKeyword: String)
