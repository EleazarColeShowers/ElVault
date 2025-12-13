package com.example.elvault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.elvault.data.enums.VaultMediaType
import com.example.elvault.data.local.entity.VaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_table ORDER BY dateAdded DESC")
    fun getVaultItems(): Flow<List<VaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(vaultEntity: VaultEntity)

    @Delete
    suspend fun deleteVaultItem(vaultEntity: VaultEntity)

    @Query("SELECT * FROM vault_table WHERE id = :id")
    suspend fun getVaultItemById(id: Int): VaultEntity?

    @Query("SELECT * FROM vault_table WHERE mediaType = :mediaType ORDER BY dateAdded DESC")
    fun getVaultItemsByType(mediaType: VaultMediaType): Flow<List<VaultEntity>>
}