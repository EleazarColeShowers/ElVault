package com.example.elvault.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.elvault.data.local.database.VaultDatabase
import com.example.elvault.data.local.entity.VaultEntity
import com.example.elvault.data.enums.VaultMediaType
import com.example.elvault.data.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.filter

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VaultRepository
    val allVaultItems: StateFlow<List<VaultEntity>>

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val vaultDao = VaultDatabase.getDatabase(application).vaultDao()
        repository = VaultRepository(vaultDao)

        val itemsFlow = MutableStateFlow<List<VaultEntity>>(emptyList())
        allVaultItems = itemsFlow.asStateFlow()

        viewModelScope.launch {
            repository.allVaultItems.collect { items ->
                itemsFlow.value = items
            }
        }
    }

    fun insertVaultItem(vaultEntity: VaultEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            repository.insertVaultItem(vaultEntity)
            _isLoading.value = false
        }
    }

    fun deleteVaultItem(vaultEntity: VaultEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVaultItem(vaultEntity)
        }
    }

    fun getVaultItemById(id: Int, onResult: (VaultEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = repository.getVaultItemById(id)
            onResult(item)
        }
    }

    fun filterByMediaType(mediaType: VaultMediaType?): List<VaultEntity> {
        return if (mediaType == null) {
            allVaultItems.value
        } else {
            allVaultItems.value.filter { it.mediaType == mediaType }
        }
    }

    fun searchVaultItems(query: String): List<VaultEntity> {
        return allVaultItems.value.filter { item ->
            item.title?.contains(query, ignoreCase = true) == true ||
                    item.description?.contains(query, ignoreCase = true) == true
        }
    }
}