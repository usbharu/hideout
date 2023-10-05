package dev.usbharu.hideout.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.ap.JsonLd
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.user.UserAuthServiceImpl
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.*
import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import tech.barbero.http.message.signing.HttpMessage
import tech.barbero.http.message.signing.HttpMessageSigner
import tech.barbero.http.message.signing.HttpRequest
import tech.barbero.http.message.signing.KeyMap
import java.net.URI
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.SecretKey

suspend fun HttpClient.postAp(
    urlString: String,
    username: String,
    jsonLd: JsonLd,
    objectMapper: ObjectMapper
): HttpResponse {
    jsonLd.context += "https://www.w3.org/ns/activitystreams"
    return this.post(urlString) {
        header("Accept", ContentType.Application.Activity)
        header("Content-Type", ContentType.Application.Activity)
        header("Signature", "keyId=\"$username\",algorithm=\"rsa-sha256\",headers=\"(request-target) digest date\"")
        val text = objectMapper.writeValueAsString(jsonLd)
        setBody(text)
    }
}

suspend fun HttpClient.getAp(urlString: String, username: String?): HttpResponse {
    return this.get(urlString) {
        header("Accept", ContentType.Application.Activity)
        username?.let {
            header("Signature", "keyId=\"$username\",algorithm=\"rsa-sha256\",headers=\"(request-target) host date\"")
        }
    }
}

class HttpSignaturePluginConfig {
    lateinit var keyMap: KeyMap
}

val httpSignaturePlugin: ClientPlugin<HttpSignaturePluginConfig> = createClientPlugin(
    "HttpSign",
    ::HttpSignaturePluginConfig
) {
    val keyMap = pluginConfig.keyMap
    val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
    format.timeZone = TimeZone.getTimeZone("GMT")
    onRequest { request, body ->

        request.header("Date", format.format(Date()))
        request.header("Host", request.url.host)
        println(request.bodyType)
        println(request.bodyType?.type)
        if (request.bodyType?.type == String::class) {
            println(body as String)
            println("Digest !!")
//            UserAuthService.sha256.reset()
            val digest =
                Base64.getEncoder().encodeToString(UserAuthServiceImpl.sha256.digest(body.toByteArray(Charsets.UTF_8)))
            request.headers.append("Digest", "sha-256=$digest")
        }

        if (request.headers.contains("Signature")) {
            val all = request.headers.getAll("Signature").orEmpty()
            val parameters = mutableListOf<String>()
            for (s in all) {
                s.split(",").forEach { parameters.add(it) }
            }

            val keyId = parameters.find { it.startsWith("keyId") }
                .orEmpty()
                .split("=")[1]
                .replace("\"", "")
            val algorithm =
                parameters.find { it.startsWith("algorithm") }
                    .orEmpty()
                    .split("=")[1]
                    .replace("\"", "")
            val headers = parameters.find { it.startsWith("headers") }
                .orEmpty()
                .split("=")[1]
                .replace("\"", "")
                .split(" ")
                .toMutableList()

            val algorithmType = when (algorithm) {
                "rsa-sha256" -> {
                    HttpMessageSigner.Algorithm.RSA_SHA256
                }

                else -> {
                    TODO()
                }
            }

            headers.map {
                when (it) {
                    "(request-target)" -> {
                        HttpMessageSigner.REQUEST_TARGET
                    }

                    "digest" -> {
                        "Digest"
                    }

                    "date" -> {
                        "Date"
                    }

                    "host" -> {
                        "Host"
                    }

                    else -> {
                        it
                    }
                }
            }

            val builder = HttpMessageSigner.builder().algorithm(algorithmType).keyId(keyId).keyMap(keyMap)
            var tmp = builder
            headers.forEach {
                tmp = tmp.addHeaderToSign(it)
            }
            val signer = tmp.build()

            request.headers.remove("Signature")

            (signer ?: return@onRequest).sign(object : HttpMessage, HttpRequest {
                override fun headerValues(name: String?): MutableList<String> =
                    name?.let { request.headers.getAll(it) }?.toMutableList() ?: mutableListOf()

                override fun addHeader(name: String?, value: String?) {
                    val split = value?.split("=").orEmpty()
                    name?.let { request.header(it, split[0] + "=\"" + split[1].trim('"') + "\"") }
                }

                override fun method(): String = request.method.value

                override fun uri(): URI = request.url.build().toURI()
            })

            val signatureHeader = request.headers.getAll("Signature").orEmpty()
            request.headers.remove("Signature")
            signatureHeader.joinToString(",") { it.replace("; ", ",").replace(";", ",") }
                .let { request.header("Signature", it) }
        }
    }
}

class KtorKeyMap(
    private val userQueryService: UserQueryService,
    private val transaction: Transaction,
    private val applicationConfig: ApplicationConfig
) : KeyMap {
    override fun getPublicKey(keyId: String?): PublicKey = runBlocking {
        val username = (keyId ?: throw IllegalArgumentException("keyId is null")).substringBeforeLast("#pubkey")
            .substringAfterLast("/")
        val publicBytes = Base64.getDecoder().decode(
            transaction.transaction {
                userQueryService.findByNameAndDomain(
                    username,
                    applicationConfig.url.host
                ).run {
                    publicKey
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replace("\n", "")
                }
            }
        )
        val x509EncodedKeySpec = X509EncodedKeySpec(publicBytes)
        return@runBlocking KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec)
    }

    override fun getPrivateKey(keyId: String?): PrivateKey = runBlocking {
        val username = (keyId ?: throw IllegalArgumentException("keyId is null")).substringBeforeLast("#pubkey")
            .substringAfterLast("/")
        val publicBytes = Base64.getDecoder().decode(
            transaction.transaction {
                userQueryService.findByNameAndDomain(
                    username,
                    applicationConfig.url.host
                ).privateKey?.run {
                    replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replace("\n", "")
                }
            }
        )
        val x509EncodedKeySpec = PKCS8EncodedKeySpec(publicBytes)
        return@runBlocking KeyFactory.getInstance("RSA").generatePrivate(x509EncodedKeySpec)
    }

    @Suppress("NotImplementedDeclaration")
    override fun getSecretKey(keyId: String?): SecretKey = TODO("Not yet implemented")
}
