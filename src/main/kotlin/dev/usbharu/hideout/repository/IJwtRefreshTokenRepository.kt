package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken

interface IJwtRefreshTokenRepository {
    suspend fun save(token: JwtRefreshToken)

    suspend fun findById(id:Long):JwtRefreshToken?
    suspend fun findByToken(token:String):JwtRefreshToken?

}
