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
 * ViewModel pour l'écran d'accueil.
 * Gère le dernier message analysé et le lancement d'analyses manuelles.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MessageRepository

    private val _lastMessage = MutableStateFlow<AnalyzedMessageEntity?>(null)
    val lastMessage: StateFlow<AnalyzedMessageEntity?> = _lastMessage.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = MessageRepository(db.analyzedMessageDao(), application)

        // Observer le dernier message analysé
        viewModelScope.launch {
            repository.getLastMessage().collect { message ->
                _lastMessage.value = message
            }
        }
    }

    /**
     * Analyse manuelle d'un message saisi par l'utilisateur.
     */
    fun analyzeManualMessage(content: String, senderNumber: String? = null) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisError.value = null
            try {
                repository.analyzeMessage(content, senderNumber)
            } catch (e: Exception) {
                _analysisError.value = "Erreur lors de l'analyse. Veuillez réessayer."
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearError() {
        _analysisError.value = null
    }
}
