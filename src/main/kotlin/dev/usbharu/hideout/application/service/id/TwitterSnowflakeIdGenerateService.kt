package dev.usbharu.hideout.application.service.id

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

// 2010-11-04T01:42:54.657
@Suppress("MagicNumber")
@Service
@Primary
object TwitterSnowflakeIdGenerateService : SnowflakeIdGenerateService(1288834974657L)