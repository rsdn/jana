package org.rsdn.jana.ui.models

import androidx.compose.ui.text.SpanStyle

/**
 * Модель элемента для рендеринга в Compose.
 * Представляет часть сообщения после парсинга HTML
 */
sealed class ComposeElement {
    /**
     * Сегмент параграфа - часть текста с единым форматированием
     */
    sealed class ParagraphSegment {
        /**
         * Обычный текст с форматированием
         */
        data class Text(
            val text: String,
            val style: SpanStyle = SpanStyle()
        ) : ParagraphSegment()

        /**
         * Ссылка - отдельный интерактивный элемент
         */
        data class Link(
            val text: String,
            val url: String
        ) : ParagraphSegment()
    }

    /**
     * Параграф с форматированным текстом (список сегментов)
     */
    data class Paragraph(
        val segments: List<ParagraphSegment>
    ) : ComposeElement()

    /**
     * Блок кода с подсветкой
     */
    data class CodeBlock(
        val code: String,
        val language: String? = null
    ) : ComposeElement()

    /**
     * Блочная цитата
     */
    data class BlockQuote(
        val content: List<ComposeElement>
    ) : ComposeElement()

    /**
     * Изображение
     */
    data class Image(
        val url: String,
        val alt: String? = null
    ) : ComposeElement()

    /**
     * Список (маркированный или нумерованный)
     */
    data class ListBlock(
        val items: List<ComposeElement>,
        val ordered: Boolean = false
    ) : ComposeElement()

    /**
     * Таблица
     */
    data class Table(
        val rows: List<Row>
    ) : ComposeElement() {
        data class Row(
            val cells: List<Cell>
        ) {
            data class Cell(
                val content: List<ParagraphSegment>
            )
        }
    }

    /**
     * Cut/spoiler блок (сворачиваемый)
     */
    data class Cut(
        val summary: String? = null,
        val content: List<ComposeElement>
    ) : ComposeElement()

    /**
     * Горизонтальная линия
     */
    data object HorizontalRule : ComposeElement()

    /**
     * Заголовок
     */
    data class Heading(
        val segments: List<ParagraphSegment>,
        val level: Int // 1-6
    ) : ComposeElement()

    /**
     * Таглайн (подпись в конце сообщения)
     */
    data class Tagline(
        val content: List<ComposeElement>
    ) : ComposeElement()
}