package org.rsdn.jana.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Стили для рендеринга сообщений RSDN
 * Основано на CSS стилях с сайта rsdn.org
 */
data class MessageStyle(
    // Цитаты по уровням (lineQuote.levelN)
    val quoteColors: List<Color> = listOf(
        Color(0xFF137900), // level 1 - тёмно-зелёный
        Color(0xFF74B967), // level 2 - средне-зелёный
        Color(0xFF9FD095), // level 3 - светло-зелёный
    ),
    
    // Таглайн (подпись в конце сообщения)
    val taglineColor: Color = Color(0xFFD3ACAC),
    
    // Ссылки
    val linkColor: Color = Color(0xFF505000),       // a:link - оливковый
    val linkVisitedColor: Color = Color(0xFF909090), // a:visited - серый
    val linkHoverColor: Color = Color(0xFFB0B0B0),   // a:hover - светло-серый
    
    // Код (.c)
    val codeBackgroundColor: Color = Color(0xFFF4F4F4),
    val codeBorderColor: Color = Color(0xFFD0D0D0),
    val codeBorderLeftWidth: Dp = 4.dp,
    val codeFontFamily: FontFamily = FontFamily.Monospace,
    
    // Блочные цитаты (blockquote.q)
    val blockQuoteBackgroundColor: Color = Color(0xFFFFFEE7),
    val blockQuoteBorderColor: Color = Color(0xFFF2D473),
    val blockQuoteBorderWidth: Dp = 1.dp,
    val blockQuoteBorderLeftWidth: Dp = 3.dp,
    val blockQuoteCornerRadius: Dp = 4.dp,
    
    // Заголовки (h1-h6)
    val headingColor: Color = Color(0xFF4580A0),
    val headingFontSizes: List<TextUnit> = listOf(
        24.sp, // h1
        20.sp, // h2
        18.sp, // h3
        18.sp, // h4
        14.sp, // h5
        14.sp, // h6
    ),
    val headingFontWeights: List<FontWeight> = listOf(
        FontWeight.Bold,   // h1
        FontWeight.Light,  // h2
        FontWeight.Bold,   // h3
        FontWeight.Light,  // h4
        FontWeight.Bold,   // h5
        FontWeight.Light,  // h6
    ),
    
    // Таблицы
    val tableBorderColor: Color = Color(0xFFD4D4D4),
    val tableHeaderBackgroundColor: Color = Color(0xFFD4D4D4),
    val tableAlternateRowColor: Color = Color(0xFFF5F5F5),
    
    // Cut/spoiler
    val cutBackgroundColor: Color = Color(0xFFF0F0F0),
    val cutSummaryColor: Color = Color(0xFF505000),
    
    // Изображения
    val imageMaxHeight: Dp = 300.dp,
    
    // Сообщение
    val messageBorderColor: Color = Color(0xFFE0E0E0),
) {
    /**
     * Получить цвет для уровня цитаты
     */
    fun getQuoteColor(level: Int): Color {
        val index = (level - 1).coerceAtLeast(0)
        return quoteColors.getOrElse(index) { quoteColors.last() }
    }
    
    /**
     * Получить размер шрифта для заголовка
     */
    fun getHeadingFontSize(level: Int): TextUnit {
        val index = (level - 1).coerceIn(0, headingFontSizes.lastIndex)
        return headingFontSizes[index]
    }
    
    /**
     * Получить вес шрифта для заголовка
     */
    fun getHeadingFontWeight(level: Int): FontWeight {
        val index = (level - 1).coerceIn(0, headingFontWeights.lastIndex)
        return headingFontWeights[index]
    }
}

/**
 * Дефолтные стили в стиле RSDN
 */
val DefaultMessageStyle = MessageStyle()

/**
 * CompositionLocal для доступа к стилям сообщений
 */
val LocalMessageStyle: ProvidableCompositionLocal<MessageStyle> = 
    staticCompositionLocalOf { DefaultMessageStyle }

/**
 * Провайдер стилей сообщений
 */
@Composable
fun ProvideMessageStyle(
    style: MessageStyle = DefaultMessageStyle,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalMessageStyle provides style) {
        content()
    }
}