package org.rsdn.jana.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.rsdn.jana.ui.components.ForumCard
import org.rsdn.jana.ui.components.ForumGroupHeader
import org.rsdn.jana.ui.models.Forum
import org.rsdn.jana.utils.SetSaver
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ForumListScreen(
    forums: List<Forum>,
    modifier: Modifier = Modifier,
    onForumClick: (Forum) -> Unit
) {
    val expandedGroups = rememberSaveable(saver = SetSaver()) {
        mutableStateOf(setOf())
    }

    val grouped = forums.groupBy { it.groupName }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        grouped.forEach { (groupName, forumsInGroup) ->
            val isExpanded = expandedGroups.value.contains(groupName)

            stickyHeader {
                ForumGroupHeader(
                    title = groupName,
                    isExpanded = isExpanded,
                    onToggle = {
                        expandedGroups.value = if (isExpanded)
                            expandedGroups.value - groupName
                        else
                            expandedGroups.value + groupName
                    }
                )
            }

            if (isExpanded) {
                items(forumsInGroup) { forum ->
                    ForumCard(forum = forum, onClick = { onForumClick(forum) })
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}