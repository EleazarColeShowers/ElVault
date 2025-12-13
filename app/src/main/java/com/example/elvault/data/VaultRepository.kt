package com.example.elvault.data



import com.example.elvault.data.local.dao.VaultDao
import com.example.elvault.data.local.entity.VaultEntity
import com.example.elvault.data.enums.VaultMediaType
import kotlinx.coroutines.flow.Flow

class VaultRepository(private val vaultDao: VaultDao) {

    val allVaultItems: Flow<List<VaultEntity>> = vaultDao.getVaultItems()

    suspend fun insertVaultItem(vaultEntity: VaultEntity) {
        vaultDao.insertVaultItem(vaultEntity)
    }

    suspend fun deleteVaultItem(vaultEntity: VaultEntity) {
        vaultDao.deleteVaultItem(vaultEntity)
    }

    suspend fun getVaultItemById(id: Int): VaultEntity? {
        return vaultDao.getVaultItemById(id)
    }

    fun getVaultItemsByType(mediaType: VaultMediaType): Flow<List<VaultEntity>> {
        return vaultDao.getVaultItemsByType(mediaType)
    }
}

