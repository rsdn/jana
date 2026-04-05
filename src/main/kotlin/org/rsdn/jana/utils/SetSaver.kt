package org.rsdn.jana.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver

fun SetSaver() = Saver<MutableState<Set<String>>, List<String>>(
    save = { it.value.toList() },
    restore = { mutableStateOf(it.toSet()) }
)