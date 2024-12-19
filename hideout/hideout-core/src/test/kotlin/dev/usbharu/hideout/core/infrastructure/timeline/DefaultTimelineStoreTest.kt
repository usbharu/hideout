package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.config.DefaultTimelineStoreConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.filter.*
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.TestPostFactory
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.timeline.*
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectWarnFilter
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.service.filter.FilterDomainService
import dev.usbharu.hideout.core.domain.service.post.IPostReadAccessControl
import dev.usbharu.hideout.core.infrastructure.other.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.query.reactions.ReactionsQueryService
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class DefaultTimelineStoreTest {
    @InjectMocks
    lateinit var timelineStore: DefaultTimelineStore

    @Mock
    lateinit var timelineRepository: TimelineRepository

    @Mock
    lateinit var timelineRelationshipRepository: TimelineRelationshipRepository

    @Mock
    lateinit var filterRepository: FilterRepository

    @Mock
    lateinit var postRepository: PostRepository

    @Mock
    lateinit var filterDomainService: FilterDomainService

    @Mock
    lateinit var internalTimelineObjectRepository: InternalTimelineObjectRepository

    @Mock
    lateinit var userDetailRepository: UserDetailRepository

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var mediaRepository: MediaRepository

    @Mock
    lateinit var iPostReadAccessControl: IPostReadAccessControl

    @Mock
    lateinit var reactionsQueryService: ReactionsQueryService

    @Spy
    val defaultTimelineStoreConfig = DefaultTimelineStoreConfig(500)

    @Spy
    val idGenerateService = TwitterSnowflakeIdGenerateService

    @Test
    fun addPost() = runTest {
        val post = TestPostFactory.create()
        whenever(timelineRelationshipRepository.findByActorId(post.actorId)).doReturn(
            listOf(
                TimelineRelationship(
                    TimelineRelationshipId(1),
                    TimelineId(12),
                    post.actorId,
                    Visible.PUBLIC
                )
            )
        )

        whenever(timelineRepository.findByIds(listOf(TimelineId(12)))).doReturn(
            listOf(
                Timeline(
                    id = TimelineId(12),
                    userDetailId = UserDetailId(post.actorId.id),
                    name = TimelineName("timeline"),
                    visibility = TimelineVisibility.PUBLIC,
                    isSystem = false
                )
            )
        )

        val filters = listOf(
            Filter(
                id = FilterId(13),
                userDetailId = UserDetailId(post.actorId.id),
                name = FilterName("filter"),
                filterContext = setOf(FilterContext.HOME),
                filterAction = FilterAction.HIDE,
                filterKeywords = setOf(
                    FilterKeyword(FilterKeywordId(14), FilterKeywordKeyword("aa"), FilterMode.NONE)
                )
            )
        )

        whenever(filterRepository.findByUserDetailId(UserDetailId(post.actorId.id))).doReturn(filters)

        whenever(filterDomainService.apply(post, FilterContext.HOME, filters)).doReturn(
            FilteredPost(
                post, listOf(
                    FilterResult(filters.first(), "aaa")
                )
            )
        )

        timelineStore.addPost(post)

        argumentCaptor<List<TimelineObject>> {
            verify(internalTimelineObjectRepository, times(1)).saveAll(capture())
            val timelineObjectList = allValues.first()

            assertThat(timelineObjectList).allSatisfy {
                assertThat(it.postId).isEqualTo(post.id)
                assertThat(it.postActorId).isEqualTo(post.actorId)
                assertThat(it.replyId).isNull()
                assertThat(it.replyActorId).isNull()
                assertThat(it.repostId).isNull()
                assertThat(it.repostActorId).isNull()

                assertThat(it.userDetailId).isEqualTo(UserDetailId(post.actorId.id))
                assertThat(it.timelineId).isEqualTo(TimelineId(12))
                assertThat(it.warnFilters).contains(TimelineObjectWarnFilter(FilterId(13), "aaa"))
            }
        }
    }

    @Test
    fun `addPost direct投稿は追加されない`() = runTest {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)
        whenever(timelineRelationshipRepository.findByActorId(post.actorId)).doReturn(
            listOf(
                TimelineRelationship(
                    TimelineRelationshipId(1),
                    TimelineId(12),
                    post.actorId,
                    Visible.PUBLIC
                )
            )
        )

        whenever(timelineRepository.findByIds(listOf(TimelineId(12)))).doReturn(
            listOf(
                Timeline(
                    id = TimelineId(12),
                    userDetailId = UserDetailId(post.actorId.id),
                    name = TimelineName("timeline"),
                    visibility = TimelineVisibility.PUBLIC,
                    isSystem = false
                )
            )
        )

        timelineStore.addPost(post)

        argumentCaptor<List<TimelineObject>> {
            verify(internalTimelineObjectRepository, times(1)).saveAll(capture())
            val timelineObjectList = allValues.first()

            assertThat(timelineObjectList).isEmpty()
        }
    }

    @Test
    fun timelineがpublicでpostがUNLISTEDの時追加されない() = runTest {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)
        whenever(timelineRelationshipRepository.findByActorId(post.actorId)).doReturn(
            listOf(
                TimelineRelationship(
                    TimelineRelationshipId(1),
                    TimelineId(12),
                    post.actorId,
                    Visible.PUBLIC
                )
            )
        )

        whenever(timelineRepository.findByIds(listOf(TimelineId(12)))).doReturn(
            listOf(
                Timeline(
                    id = TimelineId(12),
                    userDetailId = UserDetailId(post.actorId.id),
                    name = TimelineName("timeline"),
                    visibility = TimelineVisibility.PUBLIC,
                    isSystem = false
                )
            )
        )

        timelineStore.addPost(post)

        argumentCaptor<List<TimelineObject>> {
            verify(internalTimelineObjectRepository, times(1)).saveAll(capture())
            val timelineObjectList = allValues.first()

            assertThat(timelineObjectList).isEmpty()
        }
    }

    @Test
    fun timelineがpublicでpostがFOLLOWERSの時追加されない() = runTest {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)
        whenever(timelineRelationshipRepository.findByActorId(post.actorId)).doReturn(
            listOf(
                TimelineRelationship(
                    TimelineRelationshipId(1),
                    TimelineId(12),
                    post.actorId,
                    Visible.PUBLIC
                )
            )
        )

        whenever(timelineRepository.findByIds(listOf(TimelineId(12)))).doReturn(
            listOf(
                Timeline(
                    id = TimelineId(12),
                    userDetailId = UserDetailId(post.actorId.id),
                    name = TimelineName("timeline"),
                    visibility = TimelineVisibility.PUBLIC,
                    isSystem = false
                )
            )
        )

        timelineStore.addPost(post)

        argumentCaptor<List<TimelineObject>> {
            verify(internalTimelineObjectRepository, times(1)).saveAll(capture())
            val timelineObjectList = allValues.first()

            assertThat(timelineObjectList).isEmpty()
        }
    }

    @Test
    fun timelineがUNLISTEDでpostがFOLLOWERSの時追加されない() = runTest {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)
        whenever(timelineRelationshipRepository.findByActorId(post.actorId)).doReturn(
            listOf(
                TimelineRelationship(
                    TimelineRelationshipId(1),
                    TimelineId(12),
                    post.actorId,
                    Visible.PUBLIC
                )
            )
        )

        whenever(timelineRepository.findByIds(listOf(TimelineId(12)))).doReturn(
            listOf(
                Timeline(
                    id = TimelineId(12),
                    userDetailId = UserDetailId(post.actorId.id),
                    name = TimelineName("timeline"),
                    visibility = TimelineVisibility.UNLISTED,
                    isSystem = false
                )
            )
        )

        timelineStore.addPost(post)

        argumentCaptor<List<TimelineObject>> {
            verify(internalTimelineObjectRepository, times(1)).saveAll(capture())
            val timelineObjectList = allValues.first()

            assertThat(timelineObjectList).isEmpty()
        }
    }

    @Test
    fun timelineがPRIVATEでpostがFOLLOWERSの時追加される() = runTest {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)
        whenever(timelineRelationshipRepository.findByActorId(post.actorId)).doReturn(
            listOf(
                TimelineRelationship(
                    TimelineRelationshipId(1),
                    TimelineId(12),
                    post.actorId,
                    Visible.PUBLIC
                )
            )
        )

        whenever(timelineRepository.findByIds(listOf(TimelineId(12)))).doReturn(
            listOf(
                Timeline(
                    id = TimelineId(12),
                    userDetailId = UserDetailId(post.actorId.id),
                    name = TimelineName("timeline"),
                    visibility = TimelineVisibility.PRIVATE,
                    isSystem = false
                )
            )
        )

        val filters = listOf(
            Filter(
                id = FilterId(13),
                userDetailId = UserDetailId(post.actorId.id),
                name = FilterName("filter"),
                filterContext = setOf(FilterContext.HOME),
                filterAction = FilterAction.HIDE,
                filterKeywords = setOf(
                    FilterKeyword(FilterKeywordId(14), FilterKeywordKeyword("aa"), FilterMode.NONE)
                )
            )
        )

        whenever(filterRepository.findByUserDetailId(UserDetailId(post.actorId.id))).doReturn(filters)

        whenever(filterDomainService.apply(post, FilterContext.HOME, filters)).doReturn(
            FilteredPost(
                post, listOf(
                    FilterResult(filters.first(), "aaa")
                )
            )
        )

        timelineStore.addPost(post)

        argumentCaptor<List<TimelineObject>> {
            verify(internalTimelineObjectRepository, times(1)).saveAll(capture())
            val timelineObjectList = allValues.first()

            assertThat(timelineObjectList).allSatisfy {
                assertThat(it.postId).isEqualTo(post.id)
                assertThat(it.postActorId).isEqualTo(post.actorId)
                assertThat(it.replyId).isNull()
                assertThat(it.replyActorId).isNull()
                assertThat(it.repostId).isNull()
                assertThat(it.repostActorId).isNull()

                assertThat(it.userDetailId).isEqualTo(UserDetailId(post.actorId.id))
                assertThat(it.timelineId).isEqualTo(TimelineId(12))
                assertThat(it.warnFilters).contains(TimelineObjectWarnFilter(FilterId(13), "aaa"))
            }
        }
    }

    @Test
    fun repostがあるときはRepostを含めたTimelineObjectが作成される() = runTest {
        val repost = TestPostFactory.create()
        val post = TestPostFactory.create(repostId = repost.id.id)

        whenever(postRepository.findById(repost.id)).doReturn(repost)
        whenever(timelineRelationshipRepository.findByActorId(post.actorId)).doReturn(
            listOf(
                TimelineRelationship(
                    TimelineRelationshipId(1),
                    TimelineId(12),
                    post.actorId,
                    Visible.PUBLIC
                )
            )
        )

        whenever(timelineRepository.findByIds(listOf(TimelineId(12)))).doReturn(
            listOf(
                Timeline(
                    id = TimelineId(12),
                    userDetailId = UserDetailId(post.actorId.id),
                    name = TimelineName("timeline"),
                    visibility = TimelineVisibility.PUBLIC,
                    isSystem = false
                )
            )
        )

        val filters = listOf(
            Filter(
                id = FilterId(13),
                userDetailId = UserDetailId(post.actorId.id),
                name = FilterName("filter"),
                filterContext = setOf(FilterContext.HOME),
                filterAction = FilterAction.HIDE,
                filterKeywords = setOf(
                    FilterKeyword(FilterKeywordId(14), FilterKeywordKeyword("aa"), FilterMode.NONE)
                )
            )
        )

        whenever(filterRepository.findByUserDetailId(UserDetailId(post.actorId.id))).doReturn(filters)

        whenever(filterDomainService.apply(post, FilterContext.HOME, filters)).doReturn(
            FilteredPost(
                post, listOf(
                    FilterResult(filters.first(), "aaa")
                )
            )
        )

        timelineStore.addPost(post)

        argumentCaptor<List<TimelineObject>> {
            verify(internalTimelineObjectRepository, times(1)).saveAll(capture())
            val timelineObjectList = allValues.first()

            assertThat(timelineObjectList).allSatisfy {
                assertThat(it.postId).isEqualTo(post.id)
                assertThat(it.postActorId).isEqualTo(post.actorId)
                assertThat(it.replyId).isNull()
                assertThat(it.replyActorId).isNull()
                assertThat(it.repostId).isEqualTo(repost.id)
                assertThat(it.repostActorId).isEqualTo(repost.actorId)

                assertThat(it.userDetailId).isEqualTo(UserDetailId(post.actorId.id))
                assertThat(it.timelineId).isEqualTo(TimelineId(12))
                assertThat(it.warnFilters).contains(TimelineObjectWarnFilter(FilterId(13), "aaa"))
            }
        }
    }

    @Test
    fun replyがあるときはReplyを含めたTimeineObjectが作成される() = runTest {
        val reply = TestPostFactory.create()
        val post = TestPostFactory.create(replyId = reply.id.id)

        whenever(postRepository.findById(reply.id)).doReturn(reply)
        whenever(timelineRelationshipRepository.findByActorId(post.actorId)).doReturn(
            listOf(
                TimelineRelationship(
                    TimelineRelationshipId(1),
                    TimelineId(12),
                    post.actorId,
                    Visible.PUBLIC
                )
            )
        )

        whenever(timelineRepository.findByIds(listOf(TimelineId(12)))).doReturn(
            listOf(
                Timeline(
                    id = TimelineId(12),
                    userDetailId = UserDetailId(post.actorId.id),
                    name = TimelineName("timeline"),
                    visibility = TimelineVisibility.PUBLIC,
                    isSystem = false
                )
            )
        )

        val filters = listOf(
            Filter(
                id = FilterId(13),
                userDetailId = UserDetailId(post.actorId.id),
                name = FilterName("filter"),
                filterContext = setOf(FilterContext.HOME),
                filterAction = FilterAction.HIDE,
                filterKeywords = setOf(
                    FilterKeyword(FilterKeywordId(14), FilterKeywordKeyword("aa"), FilterMode.NONE)
                )
            )
        )

        whenever(filterRepository.findByUserDetailId(UserDetailId(post.actorId.id))).doReturn(filters)

        whenever(filterDomainService.apply(post, FilterContext.HOME, filters)).doReturn(
            FilteredPost(
                post, listOf(
                    FilterResult(filters.first(), "aaa")
                )
            )
        )

        timelineStore.addPost(post)

        argumentCaptor<List<TimelineObject>> {
            verify(internalTimelineObjectRepository, times(1)).saveAll(capture())
            val timelineObjectList = allValues.first()

            assertThat(timelineObjectList).allSatisfy {
                assertThat(it.postId).isEqualTo(post.id)
                assertThat(it.postActorId).isEqualTo(post.actorId)
                assertThat(it.repostId).isNull()
                assertThat(it.repostActorId).isNull()
                assertThat(it.replyId).isEqualTo(reply.id)
                assertThat(it.replyActorId).isEqualTo(reply.actorId)

                assertThat(it.userDetailId).isEqualTo(UserDetailId(post.actorId.id))
                assertThat(it.timelineId).isEqualTo(TimelineId(12))
                assertThat(it.warnFilters).contains(TimelineObjectWarnFilter(FilterId(13), "aaa"))
            }
        }
    }
}