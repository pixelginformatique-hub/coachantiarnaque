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
 * ViewModel pour l'écran de détail d'analyse.
 */
class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MessageRepository

    private val _message = MutableStateFlow<AnalyzedMessageEntity?>(null)
    val message: StateFlow<AnalyzedMessageEntity?> = _message.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = MessageRepository(db.analyzedMessageDao(), application)
    }

    fun loadMessage(messageId: Long) {
        viewModelScope.launch {
            _message.value = repository.getMessageById(messageId)
        }
    }

    fun deleteMessage(messageId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
            onDone()
        }
    }
}
