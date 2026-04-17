package com.coachantiarnaque.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.coachantiarnaque.data.local.AnalyzedMessageEntity
import com.coachantiarnaque.data.local.AppDatabase
import com.coachantiarnaque.data.repository.MessageRepository
import com.coachantiarnaque.domain.engine.EmailAnalysisEngine
import com.coachantiarnaque.domain.engine.EmailAnalysisResult
import com.coachantiarnaque.domain.model.ResultType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran d'analyse d'email.
 */
class EmailAnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = EmailAnalysisEngine()
    private val repository: MessageRepository

    private val _result = MutableStateFlow<EmailAnalysisResult?>(null)
    val result: StateFlow<EmailAnalysisResult?> = _result.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _sharedContent = MutableStateFlow("")
    val sharedContent: StateFlow<String> = _sharedContent.asStateFlow()

    private val _analyzedContent = MutableStateFlow("")
    val analyzedContent: StateFlow<String> = _analyzedContent.asStateFlow()

    private val _analyzedSender = MutableStateFlow<String?>(null)
    val analyzedSender: StateFlow<String?> = _analyzedSender.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = MessageRepository(db.analyzedMessageDao(), application)
    }

    /**
     * Pré-remplit le contenu depuis un intent de partage.
     */
    fun setSharedContent(content: String) {
        _sharedContent.value = content
    }

    /**
     * Lance l'analyse d'un email.
     */
    fun analyzeEmail(content: String, senderEmail: String?) {
        if (content.isBlank()) {
            _error.value = "Veuillez entrer le contenu de l'email"
            return
        }

        if (!senderEmail.isNullOrBlank() && !engine.isValidEmail(senderEmail)) {
            _error.value = "L'adresse email semble incorrecte"
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _error.value = null
            _result.value = null
            _analyzedContent.value = content
            _analyzedSender.value = senderEmail

            try {
                val analysisResult = engine.analyze(content, senderEmail)
                _result.value = analysisResult

                // Sauvegarder dans l'historique
                val resultType = when (analysisResult.riskLevel) {
                    com.coachantiarnaque.domain.engine.EmailRiskLevel.LOW -> ResultType.SAFE
                    com.coachantiarnaque.domain.engine.EmailRiskLevel.MODERATE -> ResultType.SUSPICIOUS
                    com.coachantiarnaque.domain.engine.EmailRiskLevel.HIGH -> ResultType.SCAM
                }
                val entity = AnalyzedMessageEntity(
                    content = "[Email] ${content.take(200)}",
                    senderNumber = senderEmail,
                    score = analysisResult.score,
                    resultType = resultType,
                    reasons = analysisResult.reasons
                )
                repository.saveMessage(entity)
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'analyse. Veuillez réessayer."
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearResult() {
        _result.value = null
        _error.value = null
    }
}
