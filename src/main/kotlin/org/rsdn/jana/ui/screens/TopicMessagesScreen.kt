package org.rsdn.jana.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.rsdn.jana.api.RsdnApi
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.MessageDao
import org.rsdn.jana.sync.SyncManager
import org.rsdn.jana.ui.components.ErrorBanner
import org.rsdn.jana.ui.components.MessageCard
import org.rsdn.jana.ui.components.QuickReplyField
import org.rsdn.jana.ui.components.TopAppBarWithBack
import org.rsdn.jana.ui.models.*
import org.rsdn.jana.ui.utils.HtmlToComposeConverter

/**
 * Экран сообщений топика с древовидной структурой
 */
@Composable
fun TopicMessagesScreen(
    topic: Topic,
    onBack: () -> Unit,
    onReply: (Int?) -> Unit,
    db: DatabaseManager,
    syncManager: SyncManager,
    authToken: String?,
    onLinkHover: ((String?) -> Unit)? = null
) {
    val messageDao = remember { MessageDao(db) }
    val scope = rememberCoroutineScope()
    val htmlConverter = remember { HtmlToComposeConverter() }
    
    // Кеш тел сообщений: messageId -> List<ComposeElement>
    val messageBodies = remember { mutableStateMapOf<Int, List<ComposeElement>>() }
    // Загружаемые сообщения
    val loadingBodies = remember { mutableStateSetOf<Int>() }
    // Загружаемые размеры тел (для видимых сообщений)
    val loadingBodySizes = remember { mutableStateSetOf<Int>() }
    // Кеш размеров тел: messageId -> bodyLength
    val bodyLengths = remember { mutableStateMapOf<Int, Int>() }
    
    var isLoading by remember { mutableStateOf(true) }
    var rootNodes by remember { mutableStateOf<List<MessageNode>>(emptyList()) }
    val flatList = remember { SnapshotStateList<MessageNode>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Состояние сдвига глубины - для навигации по глубоким веткам
    var depthOffset by remember { mutableStateOf(0) }
    var deepBranchNodeId by remember { mutableStateOf<Int?>(null) }
    var deepBranchRootNode by remember { mutableStateOf<MessageNode?>(null) }
    
    // Карта позиций скроллинга для каждого уровня глубины
    val scrollPositions = remember { mutableMapOf<Int, Pair<Int, Int>>() }
    
    LaunchedEffect(topic.id) {
        isLoading = true
        errorMessage = null
        
        try {
            val cachedMessages = messageDao.getMessagesByTopic(topic.id)
            if (cachedMessages.isNotEmpty()) {
                rootNodes = buildMessageTree(cachedMessages)
                flatList.addAll(flattenMessageTree(rootNodes))
            }
            
            syncManager.syncTopicMessages(topic.id, 0)
            
            val messages = messageDao.getMessagesByTopic(topic.id)
            rootNodes = buildMessageTree(messages)
            flatList.clear()
            flatList.addAll(flattenMessageTree(rootNodes))
            
            expandToDepth(rootNodes, 5)
            flatList.clear()
            flatList.addAll(flattenMessageTree(rootNodes))
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки сообщений: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBarWithBack(
            title = topic.title,
            depthInfo = depthOffset,
            onBack = {
                if (depthOffset > 0) {
                    // Возврат из глубокой ветки - НЕ сворачиваем ветку, только сбрасываем текст
                    deepBranchNodeId?.let { id ->
                        resetBranchTextExpanded(rootNodes, id)
                    }
                    deepBranchNodeId = null
                    deepBranchRootNode = null
                    depthOffset = 0
                    flatList.clear()
                    flatList.addAll(flattenMessageTree(rootNodes))
                } else {
                    // Выход из топика
                    onBack()
                }
            }
        )

        // Показываем ошибку в баннере, если есть
        if (errorMessage != null) {
            ErrorBanner(
                message = errorMessage!!,
                onRetry = {
                    errorMessage = null
                    // Повторная синхронизация
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (isLoading && flatList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (flatList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сообщений нет",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            return@Column
        }

        MessageTreeList(
            flatList = flatList,
            depthOffset = depthOffset,
            scrollPositions = scrollPositions,
            messageBodies = messageBodies,
            loadingBodies = loadingBodies,
            loadingBodySizes = loadingBodySizes,
            bodyLengths = bodyLengths,
            messageDao = messageDao,
            authToken = authToken,
            htmlConverter = htmlConverter,
            onLinkHover = onLinkHover,
            onReply = onReply,
            onToggleExpand = { messageId ->
                toggleNodeExpanded(rootNodes, messageId)
                flatList.clear()
                // Если мы внутри глубокой ветки, используем flattenSubtree
                if (depthOffset > 0 && deepBranchRootNode != null) {
                    flatList.addAll(flattenSubtree(deepBranchRootNode!!))
                } else {
                    flatList.addAll(flattenMessageTree(rootNodes))
                }
            },
            onToggleTextExpand = { messageId ->
                toggleNodeTextExpanded(rootNodes, messageId)
                flatList.clear()
                // Если мы внутри глубокой ветки, используем flattenSubtree
                if (depthOffset > 0 && deepBranchRootNode != null) {
                    flatList.addAll(flattenSubtree(deepBranchRootNode!!))
                } else {
                    flatList.addAll(flattenMessageTree(rootNodes))
                }
            },
            onExpandDeepBranch = { messageId ->
                // Разворачиваем глубокую ветку и сдвигаем скролл
                val node = findNodeById(rootNodes, messageId)
                if (node != null) {
                    // Устанавливаем сдвиг глубины - этот узел станет новым корнем (depth=0)
                    depthOffset = node.depth
                    deepBranchNodeId = messageId
                    deepBranchRootNode = node
                    // Разворачиваем все узлы в ветке
                    expandAllInBranch(node)
                    // Создаём flatList только из этого узла и всех его потомков
                    flatList.clear()
                    flatList.addAll(flattenSubtree(node))
                }
            },
            onLoadBody = { messageId ->
                scope.launch {
                    loadMessageBody(
                        messageId = messageId,
                        authToken = authToken,
                        messageDao = messageDao,
                        htmlConverter = htmlConverter,
                        messageBodies = messageBodies,
                        loadingBodies = loadingBodies
                    )
                }
            },
            db = db
        )

        QuickReplyField(onSend = { onReply(null) })
    }
}

/**
 * Загрузка тела сообщения
 */
private suspend fun loadMessageBody(
    messageId: Int,
    authToken: String?,
    messageDao: MessageDao,
    htmlConverter: HtmlToComposeConverter,
    messageBodies: MutableMap<Int, List<ComposeElement>>,
    loadingBodies: MutableSet<Int>
) {
    if (loadingBodies.contains(messageId)) return
    
    loadingBodies.add(messageId)
    
    try {
        // Сначала проверяем кеш в БД
        val cachedBody = messageDao.getMessageBody(messageId)
        if (!cachedBody.isNullOrBlank()) {
            val elements = htmlConverter.convert(cachedBody)
            messageBodies[messageId] = elements
            return
        }
        
        // Загружаем с API
        val api = RsdnApi(authToken)
        val messageBodyDto = api.getMessageBody(messageId)
        api.close()
        
        val htmlBody = messageBodyDto.body?.text
        if (!htmlBody.isNullOrBlank()) {
            // Сохраняем в кеш
            messageDao.saveMessageBody(messageBodyDto)
            // Конвертируем и кешируем в памяти
            val elements = htmlConverter.convert(htmlBody)
            messageBodies[messageId] = elements
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        loadingBodies.remove(messageId)
    }
}

/**
 * Список сообщений с виртуализацией
 */
@Composable
private fun MessageTreeList(
    flatList: SnapshotStateList<MessageNode>,
    depthOffset: Int = 0,
    scrollPositions: MutableMap<Int, Pair<Int, Int>>,
    messageBodies: MutableMap<Int, List<ComposeElement>>,
    loadingBodies: MutableSet<Int>,
    loadingBodySizes: MutableSet<Int>,
    bodyLengths: MutableMap<Int, Int>,
    messageDao: MessageDao,
    authToken: String?,
    htmlConverter: HtmlToComposeConverter,
    onLinkHover: ((String?) -> Unit)?,
    onReply: (Int?) -> Unit,
    onToggleExpand: (Int) -> Unit,
    onToggleTextExpand: (Int) -> Unit,
    onExpandDeepBranch: (Int) -> Unit,
    onLoadBody: (Int) -> Unit,
    db: DatabaseManager
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Восстанавливаем позицию при смене уровня глубины
    LaunchedEffect(depthOffset) {
        val savedPosition = scrollPositions[depthOffset]
        if (savedPosition != null) {
            listState.scrollToItem(savedPosition.first, savedPosition.second)
        }
    }
    
    // Сохраняем позицию скроллинга при изменении
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset, depthOffset) {
        scrollPositions[depthOffset] = Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
    }
    
    // Загрузка тел видимых сообщений (только для размера)
    LaunchedEffect(listState.layoutInfo.visibleItemsInfo, flatList.toList()) {
        val visibleNodes = listState.layoutInfo.visibleItemsInfo.mapNotNull { info ->
            flatList.getOrNull(info.index)
        }
        
        // Загружаем тела для видимых сообщений где нет тела
        visibleNodes.forEach { node ->
            val messageId = node.message.id
            // Пропускаем если уже загружено или загружается
            if (!messageBodies.containsKey(messageId) && 
                !loadingBodies.contains(messageId) && 
                !loadingBodySizes.contains(messageId) &&
                !messageDao.hasMessageBody(messageId)) {
                // Загружаем только размер (тело сообщения)
                loadingBodySizes.add(messageId)
                scope.launch {
                    try {
                        val cachedBody = messageDao.getMessageBody(messageId)
                        if (!cachedBody.isNullOrBlank()) {
                            val elements = htmlConverter.convert(cachedBody)
                            messageBodies[messageId] = elements
                            val size = elements.sumOf { element ->
                                when (element) {
                                    is ComposeElement.Paragraph -> element.segments.sumOf { 
                                        when (it) {
                                            is ComposeElement.ParagraphSegment.Text -> it.text.length
                                            is ComposeElement.ParagraphSegment.Link -> it.text.length
                                        }
                                    }
                                    is ComposeElement.CodeBlock -> element.code.length
                                    is ComposeElement.BlockQuote -> 100
                                    is ComposeElement.ListBlock -> element.items.size * 50
                                    is ComposeElement.Table -> element.rows.sumOf { it.cells.size * 20 }
                                    else -> 50
                                }
                            }
                            bodyLengths[messageId] = size
                        } else {
                            // Загружаем с API
                            val api = RsdnApi(authToken)
                            val messageBodyDto = api.getMessageBody(messageId)
                            api.close()
                            
                            val htmlBody = messageBodyDto.body?.text
                            if (!htmlBody.isNullOrBlank()) {
                                messageDao.saveMessageBody(messageBodyDto)
                                val elements = htmlConverter.convert(htmlBody)
                                messageBodies[messageId] = elements
                                val size = htmlBody.length
                                bodyLengths[messageId] = size
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        loadingBodySizes.remove(messageId)
                    }
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = flatList,
            key = { it.message.id }
        ) { node ->
            // Если depthOffset > 0, мы внутри глубокой ветки - показываем только сообщения ветки
            if (depthOffset > 0) {
                // Внутри ветки индикаторы не нужны - все сообщения уже развёрнуты.
                // Вычисляем визуальную глубину относительно корня ветки
                MessageCard(
                    node = node,
                    depthOffset = depthOffset,
                    db = db,
                    onReply = { onReply(node.message.id) },
                    onToggleExpand = { onToggleExpand(node.message.id) },
                    onToggleTextExpand = { onToggleTextExpand(node.message.id) },
                    messageBody = messageBodies[node.message.id],
                    isLoadingBody = loadingBodies.contains(node.message.id),
                    isLoadingBodySize = loadingBodySizes.contains(node.message.id),
                    bodyLength = bodyLengths[node.message.id] ?: node.message.bodyLength,
                    onLoadBody = { onLoadBody(node.message.id) },
                    onLinkHover = onLinkHover
                )
            } else {
                // depthOffset == 0, показываем всё как обычно
                // Скрываем сообщения глубже чем MAX_DISPLAY_DEPTH (глубина 11+)
                if (node.depth > MAX_DISPLAY_DEPTH) {
                    // Не отображаем
                } else {
                    // Для узлов на глубине MAX_DISPLAY_DEPTH с потомками - передаём onExpandDeepBranch
                    val expandCallback = if (node.isDeepBranchRoot) {
                        { onExpandDeepBranch(node.message.id) }
                    } else {
                        null
                    }
                    MessageCard(
                        node = node,
                        depthOffset = depthOffset,
                        db = db,
                        onReply = { onReply(node.message.id) },
                        onToggleExpand = { onToggleExpand(node.message.id) },
                        onToggleTextExpand = { onToggleTextExpand(node.message.id) },
                        onExpandDeepBranch = expandCallback,
                        messageBody = messageBodies[node.message.id],
                        isLoadingBody = loadingBodies.contains(node.message.id),
                        isLoadingBodySize = loadingBodySizes.contains(node.message.id),
                        bodyLength = bodyLengths[node.message.id] ?: node.message.bodyLength,
                        onLoadBody = { onLoadBody(node.message.id) },
                        onLinkHover = onLinkHover
                    )
                }
            }
        }
    }
}

/**
 * Найти узел по ID
 */
private fun findNodeById(nodes: List<MessageNode>, messageId: Int): MessageNode? {
    for (node in nodes) {
        if (node.message.id == messageId) return node
        val found = findNodeById(node.children, messageId)
        if (found != null) return found
    }
    return null
}

/**
 * Сбросить isTextExpanded для ветки (без сворачивания)
 */
private fun resetBranchTextExpanded(nodes: List<MessageNode>, messageId: Int): Boolean {
    for (node in nodes) {
        if (node.message.id == messageId) {
            node.isTextExpanded.value = false
            return true
        }
        if (resetBranchTextExpanded(node.children, messageId)) {
            return true
        }
    }
    return false
}

/**
 * Преобразовать поддерево в плоский список (только этот узел и все потомки)
 * Учитывает сворачивание/разворачивание узлов
 */
private fun flattenSubtree(root: MessageNode): List<MessageNode> {
    val result = mutableListOf<MessageNode>()
    flattenSubtreeRecursive(root, result)
    return result
}

private fun flattenSubtreeRecursive(node: MessageNode, result: MutableList<MessageNode>) {
    result.add(node)
    if (node.isExpanded.value) {
        node.children.forEach { child ->
            flattenSubtreeRecursive(child, result)
        }
    }
}

/**
 * Разворачивает все узлы в ветке (для входа в глубокую ветку)
 */
private fun expandAllInBranch(node: MessageNode) {
    node.isExpanded.value = true
    node.children.forEach { child ->
        expandAllInBranch(child)
    }
}