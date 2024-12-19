package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.post.*
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailHashedPassword
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.infrastructure.factory.PostContentFactoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class UpdateLocalNoteApplicationServiceTest {
    @InjectMocks
    lateinit var service: UpdateLocalNoteApplicationService

    @Mock
    lateinit var postRepository: PostRepository

    @Mock
    lateinit var userDetailRepository: UserDetailRepository

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var postContentFactoryImpl: PostContentFactoryImpl

    @Spy
    val transaction = TestTransaction


    @Test
    fun Post主はPostを編集できる() = runTest {
        val post = TestPostFactory.create()

        whenever(postRepository.findById(post.id)).doReturn(post)
        whenever(userDetailRepository.findById(UserDetailId(1))).doReturn(
            UserDetail.create(
                UserDetailId(1), post.actorId,
                UserDetailHashedPassword("")
            )
        )
        whenever(actorRepository.findById(post.actorId)).doReturn(TestActorFactory.create(id = post.actorId.id))
        val content = PostContent("<p>test</p>", "test", emptyList())
        whenever(postContentFactoryImpl.create(eq("test"))).doReturn(content)

        service.execute(
            UpdateLocalNote(post.id.id, null, "test", false, emptyList()), LocalUser(
                post.actorId,
                UserDetailId(1),
                Acct("test", "example.com")
            )
        )

        argumentCaptor<Post> {
            verify(postRepository, times(1)).save(capture())
            val first = allValues.first()

            assertEquals(
                content, first.content
            )
        }
    }

    @Test
    fun postが見つからない場合失敗() = runTest {
        assertThrows<IllegalArgumentException> {
            service.execute(
                UpdateLocalNote(1, null, "test", false, emptyList()), LocalUser(
                    ActorId(1),
                    UserDetailId(1), Acct("test", "example.com")
                )
            )
        }
    }

    @Test
    fun post主じゃない場合失敗() = runTest {
        whenever(postRepository.findById(PostId(1))).doReturn(TestPostFactory.create(id = 1, actorId = 3))

        assertThrows<PermissionDeniedException> {
            service.execute(
                UpdateLocalNote(1, null, "test", false, emptyList()), LocalUser(
                    ActorId(1),
                    UserDetailId(1), Acct("test", "example.com")
                )
            )
        }
    }

    @Test
    fun userDetailが見つからない場合失敗() = runTest {
        whenever(postRepository.findById(PostId(1))).doReturn(TestPostFactory.create(id = 1, actorId = 1))

        assertThrows<InternalServerException> {
            service.execute(
                UpdateLocalNote(1, null, "test", false, emptyList()), LocalUser(
                    ActorId(1),
                    UserDetailId(1), Acct("test", "example.com")
                )
            )
        }

        verify(userDetailRepository, times(1)).findById(UserDetailId(1))
        verify(actorRepository, never()).findById(any())
    }

    @Test
    fun actorが見つからない場合失敗() = runTest {
        val post = TestPostFactory.create()

        whenever(postRepository.findById(post.id)).doReturn(post)
        whenever(userDetailRepository.findById(UserDetailId(1))).doReturn(
            UserDetail.create(
                UserDetailId(1), post.actorId,
                UserDetailHashedPassword("")
            )
        )


        assertThrows<InternalServerException> {
            service.execute(
                UpdateLocalNote(post.id.id, null, "test", false, emptyList()), LocalUser(
                    post.actorId,
                    UserDetailId(1),
                    Acct("test", "example.com")
                )
            )
        }
        verify(userDetailRepository, times(1)).findById(UserDetailId(1))
        verify(actorRepository, times(1)).findById(ActorId(1))
        verify(postRepository, never()).save(any())
    }
}