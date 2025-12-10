package com.example.elvault.data.local.entity

import androidx.compose.foundation.content.MediaType
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_table")
data class VaultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val uri: String,          // Content URI or file path
    val mediaType: MediaType, // IMAGE / VIDEO / AUDIO / DOCUMENT
    val title: String? = null,
    val description: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
)