@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.config.CharacterLimit
import dev.usbharu.hideout.domain.model.ap.Image
import dev.usbharu.hideout.domain.model.ap.Key
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.exception.ap.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.MediaQueryService
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.repository.PostRepository
import dev.usbharu.hideout.service.ap.APNoteServiceImpl.Companion.public
import dev.usbharu.hideout.service.ap.resource.APResourceResolveService
import dev.usbharu.hideout.service.core.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.post.PostService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyLong
import org.mockito.kotlin.*
import utils.JsonObjectMapper.objectMapper
import utils.PostBuilder
import utils.UserBuilder
import java.net.URL
import java.time.Instant


class APNoteServiceImplTest {

    val userBuilder = User.UserBuilder(CharacterLimit(), ApplicationConfig(URL("https://example.com")))
    val postBuilder = Post.PostBuilder(CharacterLimit())

    @Test
    fun `createPost 新しい投稿`() {
        val mediaQueryService = mock<MediaQueryService> {
            onBlocking { findByPostId(anyLong()) } doReturn emptyList()
        }



        runTest {
            val followers = listOf(
                userBuilder.of(
                    2L,
                    "follower",
                    "follower.example.com",
                    "followerUser",
                    "test follower user",
                    "https://follower.example.com/inbox",
                    "https://follower.example.com/outbox",
                    "https://follower.example.com",
                    "https://follower.example.com",
                    publicKey = "",
                    createdAt = Instant.now(),
                    keyId = "a"
                ), userBuilder.of(
                    3L,
                    "follower2",
                    "follower2.example.com",
                    "follower2User",
                    "test follower2 user",
                    "https://follower2.example.com/inbox",
                    "https://follower2.example.com/outbox",
                    "https://follower2.example.com",
                    "https://follower2.example.com",
                    publicKey = "",
                    createdAt = Instant.now(),
                    keyId = "a"
                )
            )
            val userQueryService = mock<UserQueryService> {
                onBlocking { findById(eq(1L)) } doReturn userBuilder.of(
                    1L,
                    "test",
                    "example.com",
                    "testUser",
                    "test user",
                    "a",
                    "https://example.com/inbox",
                    "https://example.com/outbox",
                    "https://example.com",
                    publicKey = "",
                    privateKey = "a",
                    createdAt = Instant.now(),
                    keyId = "a"
                )
            }
            val followerQueryService = mock<FollowerQueryService> {
                onBlocking { findFollowersById(eq(1L)) } doReturn followers
            }
            val jobQueueParentService = mock<JobQueueParentService>()
            val activityPubNoteService = APNoteServiceImpl(
                jobQueueParentService = jobQueueParentService,
                postRepository = mock(),
                apUserService = mock(),
                userQueryService = userQueryService,
                followerQueryService = followerQueryService,
                postQueryService = mock(),
                mediaQueryService = mediaQueryService,
                objectMapper = objectMapper,
                postService = mock(),
                apResourceResolveService = mock(),
                postBuilder = postBuilder
            )
            val postEntity = postBuilder.of(
                1L, 1L, null, "test text", 1L, Visibility.PUBLIC, "https://example.com"
            )
            activityPubNoteService.createNote(postEntity)
            verify(jobQueueParentService, times(2)).schedule(eq(DeliverPostJob), any())
        }
    }

    @Test
    fun `fetchNote(String,String) ノートが既に存在する場合はDBから取得したものを返す`() = runTest {
        val url = "https://example.com/note"
        val post = PostBuilder.of()

        val postQueryService = mock<PostQueryService> {
            onBlocking { findByUrl(eq(url)) } doReturn post
        }
        val user = UserBuilder.localUserOf(id = post.userId)
        val userQueryService = mock<UserQueryService> {
            onBlocking { findById(eq(post.userId)) } doReturn user
        }
        val apNoteServiceImpl = APNoteServiceImpl(
            jobQueueParentService = mock(),
            postRepository = mock(),
            apUserService = mock(),
            userQueryService = userQueryService,
            followerQueryService = mock(),
            postQueryService = postQueryService,
            mediaQueryService = mock(),
            objectMapper = objectMapper,
            postService = mock(),
            apResourceResolveService = mock(),
            postBuilder = Post.PostBuilder(CharacterLimit())
        )

        val actual = apNoteServiceImpl.fetchNote(url)

        val expected = Note(
            name = "Post",
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(public, user.followers),
            inReplyTo = null
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `fetchNote(String,String) ノートがDBに存在しない場合リモートに取得しにいく`() = runTest {
        val url = "https://example.com/note"
        val post = PostBuilder.of()

        val postQueryService = mock<PostQueryService> {
            onBlocking { findByUrl(eq(url)) } doThrow FailedToGetResourcesException()
            onBlocking { findByApId(eq(post.apId)) } doReturn post
        }
        val user = UserBuilder.localUserOf(id = post.userId)
        val userQueryService = mock<UserQueryService> {
            onBlocking { findById(eq(post.userId)) } doReturn user
        }
        val note = Note(
            name = "Post",
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(public, user.followers),
            inReplyTo = null
        )
        val apResourceResolveService = mock<APResourceResolveService> {
            onBlocking { resolve<Note>(eq(url), any(), isNull<Long>()) } doReturn note
        }
        val apNoteServiceImpl = APNoteServiceImpl(
            jobQueueParentService = mock(),
            postRepository = mock(),
            apUserService = mock(),
            userQueryService = userQueryService,
            followerQueryService = mock(),
            postQueryService = postQueryService,
            mediaQueryService = mock(),
            objectMapper = objectMapper,
            postService = mock(),
            apResourceResolveService = apResourceResolveService,
            postBuilder = Post.PostBuilder(CharacterLimit())
        )

        val actual = apNoteServiceImpl.fetchNote(url)

        assertEquals(note, actual)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun `fetchNote(String,String) ノートをリモートから取得した際にエラーが返ってきたらFailedToGetActivityPubResourceExceptionがthrowされる`() =
        runTest {
            val url = "https://example.com/note"
            val post = PostBuilder.of()

            val postQueryService = mock<PostQueryService> {
                onBlocking { findByUrl(eq(url)) } doThrow FailedToGetResourcesException()
                onBlocking { findByApId(eq(post.apId)) } doReturn post
            }
            val user = UserBuilder.localUserOf(id = post.userId)
            val userQueryService = mock<UserQueryService> {
                onBlocking { findById(eq(post.userId)) } doReturn user
            }
            val note = Note(
                name = "Post",
                id = post.apId,
                attributedTo = user.url,
                content = post.text,
                published = Instant.ofEpochMilli(post.createdAt).toString(),
                to = listOfNotNull(public, user.followers),
                sensitive = post.sensitive,
                cc = listOfNotNull(public, user.followers),
                inReplyTo = null
            )
            val apResourceResolveService = mock<APResourceResolveService> {
                val responseData = HttpResponseData(
                    HttpStatusCode.BadRequest,
                    GMTDate(),
                    Headers.Empty,
                    HttpProtocolVersion.HTTP_1_1,
                    NullBody,
                    Dispatchers.IO
                )
                onBlocking { resolve<Note>(eq(url), any(), isNull<Long>()) } doThrow ClientRequestException(
                    DefaultHttpResponse(
                        HttpClientCall(
                            HttpClient(), HttpRequestData(
                                Url("http://example.com"),
                                HttpMethod.Get,
                                Headers.Empty,
                                EmptyContent,
                                Job(null),
                                Attributes()
                            ), responseData
                        ), responseData
                    ), ""
                )
            }
            val apNoteServiceImpl = APNoteServiceImpl(
                jobQueueParentService = mock(),
                postRepository = mock(),
                apUserService = mock(),
                userQueryService = userQueryService,
                followerQueryService = mock(),
                postQueryService = postQueryService,
                mediaQueryService = mock(),
                objectMapper = objectMapper,
                postService = mock(),
                apResourceResolveService = apResourceResolveService,
                postBuilder = Post.PostBuilder(CharacterLimit())
            )

            assertThrows<FailedToGetActivityPubResourceException> { apNoteServiceImpl.fetchNote(url) }

        }

    @Test
    fun `fetchNote(Note,String) DBに無いNoteは保存される`() = runTest {
        val user = UserBuilder.localUserOf()
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        val post = PostBuilder.of(id = generateId, userId = user.id)
        val postQueryService = mock<PostQueryService> {
            onBlocking { findByApId(eq(post.apId)) } doThrow FailedToGetResourcesException()
        }
        val postRepository = mock<PostRepository> {
            onBlocking { generateId() } doReturn generateId
        }
        val person = Person(
            name = user.name,
            id = user.url,
            preferredUsername = user.name,
            summary = user.name,
            inbox = user.inbox,
            outbox = user.outbox,
            url = user.url,
            icon = Image(
                name = user.url + "/icon.png", mediaType = "image/png", url = user.url + "/icon.png"
            ),
            publicKey = Key(
                type = emptyList(),
                name = "Public Key",
                id = user.keyId,
                owner = user.url,
                publicKeyPem = user.publicKey
            ),
            endpoints = mapOf("sharedInbox" to "https://example.com/inbox"),
            following = user.following,
            followers = user.followers
        )
        val apUserService = mock<APUserService> {
            onBlocking { fetchPersonWithEntity(eq(user.url), anyOrNull()) } doReturn (person to user)
        }
        val postService = mock<PostService>()
        val apNoteServiceImpl = APNoteServiceImpl(
            jobQueueParentService = mock(),
            postRepository = postRepository,
            apUserService = apUserService,
            userQueryService = mock(),
            followerQueryService = mock(),
            postQueryService = postQueryService,
            mediaQueryService = mock(),
            objectMapper = objectMapper,
            postService = postService,
            apResourceResolveService = mock(),
            postBuilder = postBuilder
        )

        val note = Note(
            name = "Post",
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(public, user.followers),
            inReplyTo = null
        )


        val fetchNote = apNoteServiceImpl.fetchNote(note, null)
        verify(postService, times(1)).createRemote(
            eq(
                PostBuilder.of(
                    id = generateId, userId = user.id, createdAt = post.createdAt
                )
            )
        )
        assertEquals(note, fetchNote)
    }

    @Test
    fun `fetchNote DBに存在する場合取得して返す`() = runTest {

        val user = UserBuilder.localUserOf()
        val post = PostBuilder.of(userId = user.id)

        val postQueryService = mock<PostQueryService> {
            onBlocking { findByApId(eq(post.apId)) } doReturn post
        }
        val userQueryService = mock<UserQueryService> {
            onBlocking { findById(eq(user.id)) } doReturn user
        }
        val apNoteServiceImpl = APNoteServiceImpl(
            jobQueueParentService = mock(),
            postRepository = mock(),
            apUserService = mock(),
            userQueryService = userQueryService,
            followerQueryService = mock(),
            postQueryService = postQueryService,
            mediaQueryService = mock(),
            objectMapper = objectMapper,
            postService = mock(),
            apResourceResolveService = mock(),
            postBuilder = postBuilder
        )

        val note = Note(
            name = "Post",
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(public, user.followers),
            inReplyTo = null
        )

        val fetchNote = apNoteServiceImpl.fetchNote(note, null)
        assertEquals(note, fetchNote)
    }


}
