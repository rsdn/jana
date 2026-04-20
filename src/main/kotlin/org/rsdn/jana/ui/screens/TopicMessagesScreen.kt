package org.rsdn.jana.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.MessageDao
import org.rsdn.jana.sync.SyncManager
import org.rsdn.jana.ui.components.*
import org.rsdn.jana.ui.models.*

/**
 * Экран сообщений топика с древовидной структурой
 */
@Composable
fun TopicMessagesScreen(
    topic: Topic,
    onBack: () -> Unit,
    onReply: (Int?) -> Unit,
    db: DatabaseManager,
    syncManager: SyncManager
) {
    val messageDao = remember { MessageDao(db) }
    
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
            onBack = onBack
        )

        // Плавающий индикатор сдвинутого скролла
        if (depthOffset > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            // Возврат к корню - НЕ сворачиваем ветку, только сбрасываем текст
                            deepBranchNodeId?.let { id ->
                                resetBranchTextExpanded(rootNodes, id)
                            }
                            deepBranchNodeId = null
                            deepBranchRootNode = null
                            depthOffset = 0
                            flatList.clear()
                            flatList.addAll(flattenMessageTree(rootNodes))
                        }
                    ) {
                        Text("◀ Вернуться к началу")
                    }
                    Text(
                        text = "Уровень $depthOffset+",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
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

        if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
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
            }
        )

        QuickReplyField(onSend = { onReply(null) })
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
    onReply: (Int?) -> Unit,
    onToggleExpand: (Int) -> Unit,
    onToggleTextExpand: (Int) -> Unit,
    onExpandDeepBranch: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    
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
                    onReply = { onReply(node.message.id) },
                    onToggleExpand = { onToggleExpand(node.message.id) },
                    onToggleTextExpand = { onToggleTextExpand(node.message.id) }
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
                        onReply = { onReply(node.message.id) },
                        onToggleExpand = { onToggleExpand(node.message.id) },
                        onToggleTextExpand = { onToggleTextExpand(node.message.id) },
                        onExpandDeepBranch = expandCallback
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
