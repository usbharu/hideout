/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.infrastructure.other

import dev.usbharu.hideout.core.domain.service.post.FormattedPostContent
import dev.usbharu.hideout.core.domain.service.post.PostContentFormatter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import org.owasp.html.PolicyFactory
import org.springframework.stereotype.Service

@Service
class DefaultPostContentFormatter(private val policyFactory: PolicyFactory) : PostContentFormatter {
    override fun format(content: String): FormattedPostContent {
        // まず不正なHTMLを整形する
        val document = Jsoup.parseBodyFragment(content)
        val outputSettings = Document.OutputSettings()
        outputSettings.prettyPrint(false)

        document.outputSettings(outputSettings)

        val unsafeElement = document.getElementsByTag("body").first() ?: return FormattedPostContent(
            "",
            ""
        )

        // 文字だけのHTMLなどはここでpタグで囲む
        val flattenHtml = unsafeElement.childNodes().mapNotNull {
            if (it is Element) {
                it
            } else if (it is TextNode) {
                Element("p").appendText(it.text())
            } else {
                null
            }
        }.filter { it.text().isNotBlank() }

        // HTMLのサニタイズをする
        val unsafeHtml = Elements(flattenHtml).outerHtml()

        val safeHtml = policyFactory.sanitize(unsafeHtml)

        val safeDocument =
            Jsoup.parseBodyFragment(safeHtml).getElementsByTag("body").first() ?: return FormattedPostContent("", "")

        val formattedHtml = mutableListOf<Element>()

        // 連続するbrタグを段落に変換する
        for (element in safeDocument.children()) {
            var brCount = 0
            var prevIndex = 0
            val childNodes = element.childNodes()
            for ((index, childNode) in childNodes.withIndex()) {
                if (childNode is Element && childNode.tagName() == "br") {
                    brCount++
                } else if (brCount >= 2) {
                    formattedHtml.add(
                        Element(element.tag(), element.baseUri(), element.attributes()).appendChildren(
                            childNodes.subList(
                                prevIndex,
                                index - brCount
                            )
                        )
                    )
                    prevIndex = index
                }
            }
            formattedHtml.add(
                Element(element.tag(), element.baseUri(), element.attributes()).appendChildren(
                    childNodes.subList(
                        prevIndex,
                        childNodes.size
                    )
                )
            )
        }

        val elements = Elements(formattedHtml)
        val document1 = Document("")
        document1.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
        document1.insertChildren(0, elements)

        return FormattedPostContent(elements.outerHtml().replace("\n", ""), printHtml(elements))
    }

    private fun printHtml(element: Elements): String {
        return element.joinToString("\n\n") {
            it.childNodes().joinToString("") { node ->
                if (node is Element && node.tagName() == "br") {
                    "\n"
                } else if (node is Element) {
                    node.text()
                } else if (node is TextNode) {
                    node.text()
                } else {
                    ""
                }
            }
        }
    }
}
