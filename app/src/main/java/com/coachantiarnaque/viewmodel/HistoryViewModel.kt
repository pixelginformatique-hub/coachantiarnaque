package com.coachantiarnaque.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.coachantiarnaque.data.local.AnalyzedMessageEntity
import com.coachantiarnaque.data.local.AppDatabase
import com.coachantiarnaque.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran historique.
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MessageRepository

    private val _messages = MutableStateFlow<List<AnalyzedMessageEntity>>(emptyList())
    val messages: StateFlow<List<AnalyzedMessageEntity>> = _messages.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = MessageRepository(db.analyzedMessageDao(), application)

        viewModelScope.launch {
            repository.getRecentMessages().collect { list ->
                _messages.value = list
            }
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }
}
