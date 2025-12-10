package com.example.elvault.data.models

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.elvault.data.enums.VaultCategory

data class VaultItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: VaultCategory
)