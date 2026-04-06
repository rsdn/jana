package org.rsdn.jana.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.resources.*

@Composable
fun MainBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(painterResource(Res.drawable.ic_list), null) },
            label = { Text("Форумы") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(painterResource(Res.drawable.ic_bookmark), null) },
            label = { Text("Избранное") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(painterResource(Res.drawable.ic_outbox), null) },
            label = { Text("Исходящие") }
        )
    }
}