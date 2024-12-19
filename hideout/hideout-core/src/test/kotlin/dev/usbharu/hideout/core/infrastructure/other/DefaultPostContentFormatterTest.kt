package dev.usbharu.hideout.core.infrastructure.other

import dev.usbharu.hideout.core.config.HtmlSanitizeConfig
import dev.usbharu.hideout.core.domain.service.post.FormattedPostContent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class DefaultPostContentFormatterTest {
    @InjectMocks
    lateinit var formatter: DefaultPostContentFormatter

    @Spy
    val policyFactory = HtmlSanitizeConfig().policy()

    @Test
    fun 文字だけのHTMLをPで囲む() {
        val actual = formatter.format("a")

        assertEquals(FormattedPostContent("<p>a</p>", "a"), actual)
    }

    @Test
    fun エレメントはそのまま() {
        val actual = formatter.format("<p>a</p>")

        assertEquals(FormattedPostContent("<p>a</p>", "a"), actual)
    }

    @Test
    fun コメントは無視() {
        val actual = formatter.format("<!-- aa -->")

        assertEquals(FormattedPostContent("", ""), actual)
    }

    @Test
    fun brタグを改行に() {
        val actual = formatter.format("<p>a<br></p>")

        assertEquals(FormattedPostContent("<p>a<br /></p>", "a\n"), actual)
    }

    @Test
    fun brタグ2連続を段落に() {
        val format = formatter.format("<p>a<br><br>a</p>")

        assertEquals(FormattedPostContent("<p>a</p><p>a</p>", "a\n\na"), format)
    }

    @Test
    fun brタグ3連続以上を段落にして改行2つに変換() {
        val format = formatter.format("<p>a<br><br><br>a</p>")

        assertEquals(FormattedPostContent("<p>a</p><p>a</p>", "a\n\na"), format)
    }

    @Test
    fun aタグは許可される() {
        val format = formatter.format("<a href=\"https://example.com\">p</a>")

        assertEquals(FormattedPostContent("<a href=\"https://example.com\">p</a>", "p"), format)
    }

    @Test
    fun pの中のaタグも許可される() {
        val actual = formatter.format("<p><a href=\"https://example.com\">a</a></p>")

        assertEquals(FormattedPostContent("<p><a href=\"https://example.com\">a</a></p>", "a"), actual)
    }
}