package org.rsdn.jana.ui.utils

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.rsdn.jana.ui.models.ComposeElement
import org.rsdn.jana.ui.theme.MessageStyle
import java.net.URLDecoder

/**
 * Конвертер HTML в модель для Compose рендеринга
 * Ориентируется только на CSS-классы, не парсит inline-стили
 */
class HtmlToComposeConverter(
    private val style: MessageStyle = MessageStyle()
) {
    /**
     * Конвертировать HTML в список элементов Compose
     */
    fun convert(html: String): List<ComposeElement> {
        val doc = Jsoup.parse(html)
        return parseNodes(doc.body().childNodes())
    }

    /**
     * Парсинг списка узлов
     */
    private fun parseNodes(nodes: List<Node>): List<ComposeElement> {
        val result = mutableListOf<ComposeElement>()
        val currentParagraphNodes = mutableListOf<Node>()

        for (node in nodes) {
            when (node) {
                is TextNode -> {
                    if (node.text().isNotBlank()) {
                        currentParagraphNodes.add(node)
                    }
                }
                is Element -> {
                    val tagName = node.tagName().lowercase()
                    when (tagName) {
                        // Блочные элементы - завершаем текущий параграф
                        "p", "div", "section", "article" -> {
                            if (currentParagraphNodes.isNotEmpty()) {
                                result.add(buildParagraph(currentParagraphNodes))
                                currentParagraphNodes.clear()
                            }
                            // Проверяем на tagline по CSS-классу
                            val classAttr = node.attr("class")
                            if (classAttr.contains("tagline", ignoreCase = true)) {
                                result.add(parseTagline(node))
                            } else {
                                result.addAll(parseElement(node))
                            }
                        }
                        "blockquote" -> {
                            if (currentParagraphNodes.isNotEmpty()) {
                                result.add(buildParagraph(currentParagraphNodes))
                                currentParagraphNodes.clear()
                            }
                            result.add(parseBlockQuote(node))
                        }
                        "pre" -> {
                            if (currentParagraphNodes.isNotEmpty()) {
                                result.add(buildParagraph(currentParagraphNodes))
                                currentParagraphNodes.clear()
                            }
                            result.add(parseCodeBlock(node))
                        }
                        "ul", "ol" -> {
                            if (currentParagraphNodes.isNotEmpty()) {
                                result.add(buildParagraph(currentParagraphNodes))
                                currentParagraphNodes.clear()
                            }
                            result.add(parseList(node))
                        }
                        "table" -> {
                            if (currentParagraphNodes.isNotEmpty()) {
                                result.add(buildParagraph(currentParagraphNodes))
                                currentParagraphNodes.clear()
                            }
                            result.add(parseTable(node))
                        }
                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            if (currentParagraphNodes.isNotEmpty()) {
                                result.add(buildParagraph(currentParagraphNodes))
                                currentParagraphNodes.clear()
                            }
                            result.add(parseHeading(node))
                        }
                        "hr" -> {
                            if (currentParagraphNodes.isNotEmpty()) {
                                result.add(buildParagraph(currentParagraphNodes))
                                currentParagraphNodes.clear()
                            }
                            result.add(ComposeElement.HorizontalRule)
                        }
                        "img" -> {
                            if (currentParagraphNodes.isNotEmpty()) {
                                result.add(buildParagraph(currentParagraphNodes))
                                currentParagraphNodes.clear()
                            }
                            result.add(parseImage(node))
                        }
                        "br" -> {
                            currentParagraphNodes.add(node)
                        }
                        // Инлайн элементы - добавляем к текущему параграфу
                        else -> {
                            currentParagraphNodes.add(node)
                        }
                    }
                }
            }
        }

        // Завершаем последний параграф
        if (currentParagraphNodes.isNotEmpty()) {
            result.add(buildParagraph(currentParagraphNodes))
        }

        return result
    }

    /**
     * Парсинг отдельного элемента
     */
    private fun parseElement(element: Element): List<ComposeElement> {
        return parseNodes(element.childNodes())
    }

    /**
     * Построение параграфа из узлов
     */
    private fun buildParagraph(nodes: List<Node>): ComposeElement.Paragraph {
        return ComposeElement.Paragraph(buildSegmentsFromNodes(nodes))
    }

    /**
     * Построение списка сегментов из узлов
     */
    private fun buildSegmentsFromNodes(
        nodes: List<Node>,
        parentStyle: SpanStyle = SpanStyle()
    ): List<ComposeElement.ParagraphSegment> {
        val segments = mutableListOf<ComposeElement.ParagraphSegment>()

        for (node in nodes) {
            when (node) {
                is TextNode -> {
                    val text = node.text()
                    if (text.isNotBlank()) {
                        segments.add(ComposeElement.ParagraphSegment.Text(text, parentStyle))
                    }
                }
                is Element -> {
                    val tagName = node.tagName().lowercase()

                  when (tagName) {
                    "br" -> {
                      // Добавляем перенос строки как текстовый сегмент
                      segments.add(ComposeElement.ParagraphSegment.Text("\n", parentStyle))
                    }
                    "a" -> {
                      // Ссылка - отдельный сегмент
                      val href = node.attr("href")
                      val rawText = node.text()
                      val text = try {
                        URLDecoder.decode(rawText, "UTF-8")
                      } catch (_: Exception) {
                        rawText
                      }
                      segments.add(ComposeElement.ParagraphSegment.Link(text, href))
                    }
                    else -> {
                      // Получаем стиль для элемента
                      val newStyle = getStyleForElement(node, parentStyle)
                      // Рекурсивно обрабатываем дочерние узлы
                      val childSegments = buildSegmentsFromNodes(node.childNodes(), newStyle)
                      segments.addAll(childSegments)
                    }
                  }
                }
            }
        }

        return segments
    }

    /**
     * Получение стиля для элемента по тегу и CSS-классу
     */
    private fun getStyleForElement(element: Element, baseStyle: SpanStyle): SpanStyle {
        var style = baseStyle
        val tagName = element.tagName().lowercase()
        val classAttr = element.attr("class")

        // Стили по тегам
        when (tagName) {
            "b", "strong" -> style = style.merge(SpanStyle(fontWeight = FontWeight.Bold))
            "i", "em" -> style = style.merge(SpanStyle(fontStyle = FontStyle.Italic))
            "u" -> style = style.merge(SpanStyle(textDecoration = TextDecoration.Underline))
            "s", "strike", "del" -> style = style.merge(SpanStyle(textDecoration = TextDecoration.LineThrough))
            "code" -> style = style.merge(SpanStyle(
                fontFamily = this@HtmlToComposeConverter.style.codeFontFamily,
                background = this@HtmlToComposeConverter.style.codeBackgroundColor
            ))
            "sub" -> style = style.merge(SpanStyle(
                fontSize = 0.7.em,
                baselineShift = BaselineShift.Subscript
            ))
            "sup" -> style = style.merge(SpanStyle(
                fontSize = 0.7.em,
                baselineShift = BaselineShift.Superscript
            ))
            "span" -> {
                // Проверяем CSS-классы
                when {
                    classAttr.contains("lineQuote", ignoreCase = true) -> {
                        val level = extractQuoteLevel(classAttr)
                        style = style.merge(SpanStyle(color = this.style.getQuoteColor(level)))
                    }
                }
            }
        }

        return style
    }

    /**
     * Извлечь уровень цитаты из CSS-класса (level1, level2, etc.)
     */
    private fun extractQuoteLevel(classAttr: String): Int {
        val match = Regex("level(\\d+)", RegexOption.IGNORE_CASE).find(classAttr)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }

    /**
     * Парсинг таглайна
     */
    private fun parseTagline(element: Element): ComposeElement.Tagline {
        return ComposeElement.Tagline(parseNodes(element.childNodes()))
    }

    /**
     * Парсинг блока кода
     */
    private fun parseCodeBlock(element: Element): ComposeElement.CodeBlock {
        val codeElement = element.selectFirst("code")
        val language = codeElement?.attr("class")?.removePrefix("language-")?.substringBefore(" ")
        val code = codeElement?.text() ?: element.text()
        return ComposeElement.CodeBlock(code, language)
    }

    /**
     * Парсинг цитаты
     */
    private fun parseBlockQuote(element: Element): ComposeElement.BlockQuote {
        return ComposeElement.BlockQuote(parseNodes(element.childNodes()))
    }

    /**
     * Парсинг списка
     */
    private fun parseList(element: Element): ComposeElement.ListBlock {
        val items = element.children()
            .filter { it.tagName().lowercase() == "li" }
            .map { li ->
                ComposeElement.Paragraph(buildSegmentsFromNodes(li.childNodes()))
            }
        val ordered = element.tagName().lowercase() == "ol"
        return ComposeElement.ListBlock(items, ordered)
    }

    /**
     * Парсинг таблицы
     */
    private fun parseTable(element: Element): ComposeElement {
        // Проверяем, является ли это cut-таблицей
        if (isCutTable(element)) {
            return parseCutTable(element)
        }
        
        val rows = element.select("tr").map { tr ->
            ComposeElement.Table.Row(
                cells = tr.children().map { cell ->
                    ComposeElement.Table.Row.Cell(
                        content = buildSegmentsFromNodes(cell.childNodes())
                    )
                }
            )
        }
        return ComposeElement.Table(rows)
    }
    
    /**
     * Проверить, является ли таблица cut-блоком
     */
    private fun isCutTable(element: Element): Boolean {
        // Проверяем по классу hidden_Minus на ячейке
        val hasHiddenMinus = element.select("td.hidden_Minus").firstOrNull() != null
        if (hasHiddenMinus) return true
        
        // Проверяем по onclick на tbody
        val tbodies = element.select("tbody")
        if (tbodies.size >= 1) {
            val firstTbody = tbodies.first()
            if (firstTbody?.attr("onclick")?.contains("toggleExpand", ignoreCase = true) == true) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Парсинг cut-таблицы
     */
    private fun parseCutTable(element: Element): ComposeElement.Cut {
        val tbodies = element.select("tbody")
        
        var summary: String? = null
        val contentElements = mutableListOf<ComposeElement>()
        
        // Первый tbody содержит summary
        if (tbodies.size >= 1) {
            val firstTbody = tbodies[0]
            // Ищем ячейку с текстом (не с &nbsp;)
            val cells = firstTbody.select("td")
            for (cell in cells) {
                val text = cell.text().trim()
                if (text.isNotEmpty() && !text.startsWith(" ")) {
                    summary = text
                    break
                }
            }
        }
        
        // Второй tbody содержит содержимое
        if (tbodies.size >= 2) {
            val secondTbody = tbodies[1]
            // Парсим содержимое tbody, пропуская декоративную линию
            val rows = secondTbody.select("tr")
            for (row in rows) {
                val cells = row.select("td")
                for ((index, cell) in cells.withIndex()) {
                    // Пропускаем первую ячейку если она декоративная (линия)
                    if (index == 0 && cell.attr("style").contains("background-image", ignoreCase = true)) {
                        continue
                    }
                    // Парсим содержимое ячейки
                    val cellContent = parseNodes(cell.childNodes())
                    contentElements.addAll(cellContent)
                }
            }
        }
        
        return ComposeElement.Cut(summary, contentElements)
    }

    /**
     * Парсинг заголовка
     */
    private fun parseHeading(element: Element): ComposeElement.Heading {
        val level = element.tagName().lowercase().removePrefix("h").toIntOrNull() ?: 1
        return ComposeElement.Heading(
            segments = buildSegmentsFromNodes(element.childNodes()),
            level = level
        )
    }

    /**
     * Парсинг изображения
     */
    private fun parseImage(element: Element): ComposeElement.Image {
        val src = element.attr("src")
        val alt = element.attr("alt").takeIf { it.isNotBlank() }
        return ComposeElement.Image(src, alt)
    }
}