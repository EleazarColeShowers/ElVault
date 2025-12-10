package com.example.elvault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.elvault.data.local.entity.VaultEntity
import com.example.elvault.data.models.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_table")
    fun getVaultItems(): Flow<List<VaultEntity>>

    @Insert
    fun insertVaultItem(vaultEntity: VaultEntity)

    @Delete
    fun deleteVaultItem(vaultEntity: VaultEntity)
}