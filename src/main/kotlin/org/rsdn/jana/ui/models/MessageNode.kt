package org.rsdn.jana.ui.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.rsdn.jana.data.models.MessageInfoDto

/**
 * Максимальная глубина отображения дерева без "туннелирования"
 */
const val MAX_DISPLAY_DEPTH = 5

/**
 * Узел дерева сообщений
 * @param message Сообщение
 * @param children Дочерние сообщения
 * @param depth Глубина вложенности (0 - корневое сообщение топика)
 * @param isExpanded Развёрнут ли узел (для UI) - показывает дочерние сообщения
 * @param isTextExpanded Развёрнут ли текст сообщения (для UI)
 * @param pathToRoot Путь к корню для глубоких веток (используется при туннелировании)
 * @param parent Родительский узел (null для корневого)
 */
data class MessageNode(
    val message: MessageInfoDto,
    val children: MutableList<MessageNode> = mutableListOf(),
    var depth: Int = 0,
    val isExpanded: MutableState<Boolean> = mutableStateOf(true),
    val isTextExpanded: MutableState<Boolean> = mutableStateOf(false),
    var pathToRoot: List<MessageInfoDto> = emptyList(),
    var parent: MessageNode? = null
) {
    /**
     * Проверка, является ли узел "глубоким" (требует туннелирования)
     */
    val isDeepNode: Boolean get() = depth > MAX_DISPLAY_DEPTH

    /**
     * Проверка, является ли узел "пограничным" - на глубине MAX_DISPLAY_DEPTH
     * и имеет потомков (т.е. является входом в глубокую ветку)
     */
    val isDeepBranchRoot: Boolean get() = depth == MAX_DISPLAY_DEPTH && children.isNotEmpty()

    /**
     * Количество всех потомков (рекурсивно)
     */
    val totalDescendants: Int
        get() = children.sumOf { it.totalDescendants + 1 }
}

/**
 * Построить дерево сообщений из плоского списка
 * @param messages Плоский список сообщений (обычно из БД)
 * @return Список корневых узлов дерева
 */
fun buildMessageTree(messages: List<MessageInfoDto>): List<MessageNode> {
    if (messages.isEmpty()) return emptyList()

    // Мапа для быстрого доступа к узлам по ID сообщения
    val nodeMap = mutableMapOf<Int, MessageNode>()
    
    // Сначала создаём все узлы
    messages.forEach { msg ->
        nodeMap[msg.id] = MessageNode(message = msg)
    }

    // Находим корневое сообщение топика (то, у которого нет родителя или parentID == null)
    // В RSDN API у корневого сообщения topicID == id
    val rootMessage = messages.find { it.id == it.topicID } 
        ?: messages.find { it.parentID == null }
        ?: return emptyList()

    // Строим дерево, начиная с корня
    val rootNode = buildNode(rootMessage, nodeMap, messages, emptyList(), null)
    
    return listOf(rootNode)
}

/**
 * Рекурсивно построить узел и его потомков
 */
private fun buildNode(
    message: MessageInfoDto,
    nodeMap: MutableMap<Int, MessageNode>,
    allMessages: List<MessageInfoDto>,
    pathToRoot: List<MessageInfoDto>,
    parentNode: MessageNode?
): MessageNode {
    val currentPath = pathToRoot + message
    val node = nodeMap[message.id] ?: return MessageNode(message = message)
    
    // Устанавливаем родителя
    node.parent = parentNode
    
    // Находим все сообщения, у которых parentID == текущему id
    val childrenMessages = allMessages.filter { it.parentID == message.id }
    
    childrenMessages.forEach { childMsg ->
        val childNode = buildNode(childMsg, nodeMap, allMessages, currentPath, node)
        node.children.add(childNode)
    }

    // Сортируем детей по дате создания (хронологически)
    node.children.sortBy { it.message.createdOn }

    // Устанавливаем глубину и путь к корню напрямую (без copy)
    node.depth = pathToRoot.size
    node.pathToRoot = pathToRoot

    return node
}

/**
 * Преобразовать дерево в плоский список для отображения в LazyColumn.
 * Учитывает сворачивание/разворачивание узлов.
 * Узлы глубже MAX_DISPLAY_DEPTH не добавляются (они показываются через индикатор).
 */
fun flattenMessageTree(
    nodes: List<MessageNode>,
    result: MutableList<MessageNode> = mutableListOf()
): MutableList<MessageNode> {
    nodes.forEach { node ->
        // Не добавляем узлы глубже MAX_DISPLAY_DEPTH
        if (node.depth <= MAX_DISPLAY_DEPTH) {
            result.add(node)
        }
        // Продолжаем обход только если узел развёрнут И мы не достигли границы
        if (node.isExpanded.value && node.depth < MAX_DISPLAY_DEPTH) {
            flattenMessageTree(node.children, result)
        }
    }
    return result
}

/**
 * Переключить состояние развёрнутости узла
 * Состояние текста сообщения не меняется - оно сохраняется независимо
 */
fun toggleNodeExpanded(nodes: List<MessageNode>, messageId: Int): Boolean {
    for (node in nodes) {
        if (node.message.id == messageId) {
            node.isExpanded.value = !node.isExpanded.value
            return true
        }
        if (toggleNodeExpanded(node.children, messageId)) {
            return true
        }
    }
    return false
}

/**
 * Развернуть все узлы до указанной глубины
 */
fun expandToDepth(nodes: List<MessageNode>, maxDepth: Int = MAX_DISPLAY_DEPTH) {
    nodes.forEach { node ->
        expandNodeToDepth(node, maxDepth)
    }
}

private fun expandNodeToDepth(node: MessageNode, maxDepth: Int) {
    if (node.depth < maxDepth) {
        node.isExpanded.value = true
        node.children.forEach { child ->
            expandNodeToDepth(child, maxDepth)
        }
    }
}

/**
 * Переключить состояние развёрнутости текста сообщения
 */
fun toggleNodeTextExpanded(nodes: List<MessageNode>, messageId: Int): Boolean {
    for (node in nodes) {
        if (node.message.id == messageId) {
            node.isTextExpanded.value = !node.isTextExpanded.value
            return true
        }
        if (toggleNodeTextExpanded(node.children, messageId)) {
            return true
        }
    }
    return false
}

/**
 * Развернуть глубокую ветку начиная с указанного узла.
 * Помечает узел как "полностью развёрнутый" чтобы скрыть индикатор
 */
fun expandDeepBranch(nodes: List<MessageNode>, messageId: Int): Boolean {
    for (node in nodes) {
        if (node.message.id == messageId) {
            // Разворачиваем этот узел и всех его потомков
            expandNodeAndDescendants(node)
            // Помечаем что эта ветка полностью развёрнута
            node.isTextExpanded.value = true
            return true
        }
        if (expandDeepBranch(node.children, messageId)) {
            return true
        }
    }
    return false
}

private fun expandNodeAndDescendants(node: MessageNode) {
    node.isExpanded.value = true
    node.children.forEach { child ->
        expandNodeAndDescendants(child)
    }
}