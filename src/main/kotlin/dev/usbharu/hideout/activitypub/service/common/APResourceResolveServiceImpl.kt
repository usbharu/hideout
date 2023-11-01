package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import org.springframework.stereotype.Service

@Service
class APResourceResolveServiceImpl(
    private val apRequestService: APRequestService,
    private val userRepository: UserRepository,
    private val cacheManager: CacheManager
) :
    APResourceResolveService {

    override suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singerId: Long?): T =
        internalResolve(url, singerId, clazz)

    override suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singer: User?): T =
        internalResolve(url, singer, clazz)

    private suspend fun <T : Object> internalResolve(url: String, singerId: Long?, clazz: Class<T>): T {
        val key = genCacheKey(url, singerId)

        cacheManager.putCache(key) {
            runResolve(url, singerId?.let { userRepository.findById(it) }, clazz)
        }
        return cacheManager.getOrWait(key) as T
    }

    private suspend fun <T : Object> internalResolve(url: String, singer: User?, clazz: Class<T>): T {
        val key = genCacheKey(url, singer?.id)
        cacheManager.putCache(key) {
            runResolve(url, singer, clazz)
        }
        return cacheManager.getOrWait(key) as T
    }

    private suspend fun <T : Object> runResolve(url: String, singer: User?, clazz: Class<T>): Object =
        apRequestService.apGet(url, singer, clazz)

    private fun genCacheKey(url: String, singerId: Long?): String {
        if (singerId != null) {
            return "$url-$singerId"
        }
        return url
    }
}
