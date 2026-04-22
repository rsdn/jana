 package org.rsdn.jana.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.data.VisitedLinksStore
import org.rsdn.jana.data.rememberVisitedLinks
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.ic_expand_less
import org.rsdn.jana.resources.ic_expand_more
import org.rsdn.jana.ui.models.ComposeElement
import org.rsdn.jana.ui.theme.LocalMessageStyle
import org.rsdn.jana.ui.theme.MessageStyle

 private const val URL_TAG = "URL"

/**
 * Рендеринг тела сообщения
 */
@Composable
fun MessageBodyView(
    elements: List<ComposeElement>,
    modifier: Modifier = Modifier,
    onLinkHover: ((String?) -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val style = LocalMessageStyle.current
    val visitedLinksStore = rememberVisitedLinks()

    SelectionContainer {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            elements.forEach { element ->
                RenderElement(
                    element = element,
                    uriHandler = uriHandler,
                    style = style,
                    visitedLinksStore = visitedLinksStore,
                    onLinkHover = onLinkHover
                )
            }
        }
    }
}

@Composable
private fun RenderElement(
    element: ComposeElement,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    style: MessageStyle,
    visitedLinksStore: VisitedLinksStore,
    onLinkHover: ((String?) -> Unit)?
) {
    when (element) {
        is ComposeElement.Paragraph -> ParagraphView(
            paragraph = element,
            visitedLinksStore = visitedLinksStore,
            onLinkHover = onLinkHover
        )
        is ComposeElement.CodeBlock -> CodeBlockView(element, style)
        is ComposeElement.BlockQuote -> BlockQuoteView(element, uriHandler, style, visitedLinksStore, onLinkHover)
        is ComposeElement.Image -> ImageView(element, style)
        is ComposeElement.ListBlock -> ListView(element, uriHandler, style, visitedLinksStore, onLinkHover)
        is ComposeElement.Table -> TableView(element, style, visitedLinksStore, onLinkHover)
        is ComposeElement.Cut -> CutView(element, uriHandler, style, visitedLinksStore, onLinkHover)
        is ComposeElement.HorizontalRule -> HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        is ComposeElement.Heading -> HeadingView(element, style, visitedLinksStore, onLinkHover)
        is ComposeElement.Tagline -> TaglineView(element, uriHandler, style, visitedLinksStore, onLinkHover)
    }
}

/**
 * Таглайн (подпись в конце сообщения)
 */
@Composable
private fun TaglineView(
    tagline: ComposeElement.Tagline,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    style: MessageStyle,
    visitedLinksStore: VisitedLinksStore,
    onLinkHover: ((String?) -> Unit)?
) {
    val taglineColor = style.taglineColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tagline.content.forEach { element ->
            CompositionLocalProvider(LocalContentColor provides taglineColor) {
                RenderElement(element, uriHandler, style, visitedLinksStore, onLinkHover)
            }
        }
    }
}

/**
 * Параграф с сегментами (текст и ссылки) - использует AnnotatedString
 */
@Composable
private fun ParagraphView(
    paragraph: ComposeElement.Paragraph,
    visitedLinksStore: VisitedLinksStore,
    onLinkHover: ((String?) -> Unit)?
) {
    if (paragraph.segments.isEmpty()) return
    
    val uriHandler = LocalUriHandler.current
    val style = LocalMessageStyle.current
    
    // Получаем список посещённых URL
    val visitedUrls by visitedLinksStore.visitedUrls.collectAsState()
    
    // Текстовый цвет из контекста (учитывает LocalContentColor для tagline)
    val textColor = LocalContentColor.current
    
    // Строим AnnotatedString со всеми сегментами (для определения позиций ссылок)
    val annotatedString = buildAnnotatedStringFromSegments(
        segments = paragraph.segments,
        defaultColor = textColor,
        visitedUrls = visitedUrls,
        hoveredUrl = null,
        style = style
    )
    
    // Проверяем, есть ли ссылки
    val hasLinks = paragraph.segments.any { it is ComposeElement.ParagraphSegment.Link }
    
    if (hasLinks) {
        // Для параграфов со ссылками - интерактивный текст
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        var hoveredUrl by remember { mutableStateOf<String?>(null) }
        
        // Уведомляем об изменении hoveredUrl
        LaunchedEffect(hoveredUrl) {
            onLinkHover?.invoke(hoveredUrl)
        }
        
        // Перестраиваем AnnotatedString при изменении hoveredUrl для подсветки
        val highlightedAnnotatedString = buildAnnotatedStringFromSegments(
            segments = paragraph.segments,
            defaultColor = textColor,
            visitedUrls = visitedUrls,
            hoveredUrl = hoveredUrl,
            style = style
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event: PointerEvent = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move -> {
                                    val position = event.changes.firstOrNull()?.position
                                    if (position != null) {
                                        textLayoutResult?.let { layoutResult ->
                                            val charOffset = layoutResult.getOffsetForPosition(position)
                                            val url = annotatedString.getStringAnnotations(URL_TAG, charOffset, charOffset)
                                                .firstOrNull()?.item
                                            if (url != hoveredUrl) {
                                                hoveredUrl = url
                                            }
                                        }
                                    }
                                }
                                PointerEventType.Exit -> {
                                    hoveredUrl = null
                                }
                            }
                        }
                    }
                }
        ) {
            Text(
                text = highlightedAnnotatedString,
                style = MaterialTheme.typography.bodyMedium,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier
                    .pointerHoverIcon(
                        if (hoveredUrl != null) PointerIcon.Hand else PointerIcon.Default,
                        overrideDescendants = true
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Клик - открываем URL под курсором или первую ссылку
                        val urlToOpen = hoveredUrl ?: paragraph.segments.firstNotNullOfOrNull { 
                            if (it is ComposeElement.ParagraphSegment.Link) it.url else null 
                        }
                        urlToOpen?.let { url ->
                            visitedLinksStore.markVisited(url)
                            try {
                                uriHandler.openUri(url)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            )
        }
    } else {
        // Без ссылок - просто текст
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Построение AnnotatedString из сегментов
 */
private fun buildAnnotatedStringFromSegments(
    segments: List<ComposeElement.ParagraphSegment>,
    defaultColor: Color,
    visitedUrls: Set<String>,
    hoveredUrl: String?,
    style: MessageStyle
): AnnotatedString {
    return buildAnnotatedString {
        segments.forEach { segment ->
            when (segment) {
                is ComposeElement.ParagraphSegment.Text -> {
                    val segmentColor = if (segment.style.color.isUnspecified) {
                        defaultColor
                    } else {
                        segment.style.color
                    }
                    val mergedStyle = segment.style.copy(color = segmentColor)
                    withStyle(mergedStyle) {
                        append(segment.text)
                    }
                }
                is ComposeElement.ParagraphSegment.Link -> {
                    val isVisited = segment.url in visitedUrls
                    val isHovered = segment.url == hoveredUrl
                    
                    // Подсвечиваем ссылку при hover
                    val linkColor = when {
                        isHovered -> style.linkHoverColor
                        isVisited -> style.linkVisitedColor
                        else -> style.linkColor
                    }
                    
                    // Добавляем URL как тег для отслеживания hover
                    pushStringAnnotation(URL_TAG, segment.url)
                    withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                        append(segment.text)
                    }
                    pop()
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(
    codeBlock: ComposeElement.CodeBlock,
    style: MessageStyle
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Слева снаружи - символы </>
        Text(
            text = "</>",
            style = MaterialTheme.typography.labelSmall,
            color = style.codeBorderColor,
            modifier = Modifier
                .width(24.dp)
                .padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Рамка с кодом
        Surface(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, style.codeBorderColor, RoundedCornerShape(4.dp)),
            shape = RoundedCornerShape(4.dp),
            color = style.codeBackgroundColor
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!codeBlock.language.isNullOrBlank()) {
                    Text(
                        text = codeBlock.language.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                SelectionContainer {
                    Text(
                        text = codeBlock.code,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = style.codeFontFamily
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockQuoteView(
    blockQuote: ComposeElement.BlockQuote,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    style: MessageStyle,
    visitedLinksStore: VisitedLinksStore,
    onLinkHover: ((String?) -> Unit)?
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Слева снаружи - кавычка
        Text(
            text = "\"",
            style = MaterialTheme.typography.headlineMedium,
            color = style.blockQuoteBorderColor,
            modifier = Modifier
                .width(20.dp)
                .padding(top = 0.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Рамка с цитатой
        Surface(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = style.blockQuoteBorderWidth,
                    color = style.blockQuoteBorderColor,
                    shape = RoundedCornerShape(style.blockQuoteCornerRadius)
                ),
            shape = RoundedCornerShape(style.blockQuoteCornerRadius),
            color = style.blockQuoteBackgroundColor
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                blockQuote.content.forEach { element ->
                    RenderElement(element, uriHandler, style, visitedLinksStore, onLinkHover)
                }
            }
        }
    }
}

@Composable
private fun ImageView(
    image: ComposeElement.Image,
    style: MessageStyle
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        AsyncImage(
            model = image.url,
            contentDescription = image.alt ?: "Image",
            modifier = Modifier
                .heightIn(max = style.imageMaxHeight)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ListView(
    listBlock: ComposeElement.ListBlock,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    style: MessageStyle,
    visitedLinksStore: VisitedLinksStore,
    onLinkHover: ((String?) -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listBlock.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = if (listBlock.ordered) "${index + 1}." else "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(24.dp)
                )
                when (item) {
                    is ComposeElement.Paragraph -> ParagraphView(item, visitedLinksStore, onLinkHover)
                    else -> RenderElement(item, uriHandler, style, visitedLinksStore, onLinkHover)
                }
            }
        }
    }
}

@Composable
private fun TableView(
    table: ComposeElement.Table,
    style: MessageStyle,
    visitedLinksStore: VisitedLinksStore,
    onLinkHover: ((String?) -> Unit)?
) {
    val textColor = LocalContentColor.current
    val visitedUrls by visitedLinksStore.visitedUrls.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, style.tableBorderColor, RoundedCornerShape(8.dp))
    ) {
        table.rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (rowIndex % 2 == 0) MaterialTheme.colorScheme.surface
                        else style.tableAlternateRowColor
                    )
            ) {
                row.cells.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        val annotatedString = buildAnnotatedStringFromSegments(
                            segments = cell.content,
                            defaultColor = textColor,
                            visitedUrls = visitedUrls,
                            hoveredUrl = null,
                            style = style
                        )
                        Text(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            if (rowIndex < table.rows.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = style.tableBorderColor
                )
            }
        }
    }
}

@Composable
private fun CutView(
    cut: ComposeElement.Cut,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    style: MessageStyle,
    visitedLinksStore: VisitedLinksStore,
    onLinkHover: ((String?) -> Unit)?
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = style.cutBackgroundColor
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        if (isExpanded) Res.drawable.ic_expand_less else Res.drawable.ic_expand_more
                    ),
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    tint = style.cutSummaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = cut.summary ?: if (isExpanded) "Свернуть" else "Показать скрытое содержимое",
                    style = MaterialTheme.typography.labelMedium,
                    color = style.cutSummaryColor
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                cut.content.forEach { element ->
                    RenderElement(element, uriHandler, style, visitedLinksStore, onLinkHover)
                }
            }
        }
    }
}

@Composable
private fun HeadingView(
    heading: ComposeElement.Heading,
    style: MessageStyle,
    visitedLinksStore: VisitedLinksStore,
    onLinkHover: ((String?) -> Unit)?
) {
    val fontSize = style.getHeadingFontSize(heading.level)
    val fontWeight = style.getHeadingFontWeight(heading.level)
    val visitedUrls by visitedLinksStore.visitedUrls.collectAsState()
    
    val annotatedString = buildAnnotatedStringFromSegments(
        segments = heading.segments,
        defaultColor = style.headingColor,
        visitedUrls = visitedUrls,
        hoveredUrl = null,
        style = style
    )
    
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = fontSize,
            fontWeight = fontWeight
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}