package com.coachantiarnaque.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.coachantiarnaque.BuildConfig
import com.coachantiarnaque.data.api.*
import com.coachantiarnaque.data.local.AnalyzedMessageDao
import com.coachantiarnaque.data.local.AnalyzedMessageEntity
import com.coachantiarnaque.domain.engine.ApiCheckResults
import com.coachantiarnaque.domain.engine.ScamDetectionEngine
import com.coachantiarnaque.domain.model.AnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository central gérant l'analyse des messages,
 * les appels API et le stockage local.
 */
class MessageRepository(
    private val dao: AnalyzedMessageDao,
    private val context: Context
) {
    private val engine = ScamDetectionEngine()

    /**
     * Analyse un message complet : règles locales + APIs externes.
     */
    suspend fun analyzeMessage(
        content: String,
        senderNumber: String? = null
    ): AnalyzedMessageEntity = withContext(Dispatchers.IO) {

        // Récupérer les contacts connus
        val knownContacts = getKnownContacts()

        // Extraire les URLs du message
        val urls = ScamDetectionEngine.URL_REGEX
            .findAll(content)
            .map { it.value }
            .toList()

        // Vérifications API externes
        val apiResults = checkApis(urls, senderNumber)

        // Analyse avec le moteur de détection
        val result = engine.analyze(
            content = content,
            senderNumber = senderNumber,
            knownContacts = knownContacts,
            apiResults = apiResults
        )

        // Sauvegarder en base
        val entity = AnalyzedMessageEntity(
            content = content,
            senderNumber = senderNumber,
            score = result.score,
            resultType = result.resultType,
            reasons = result.reasons
        )
        val id = dao.insert(entity)
        entity.copy(id = id)
    }

    /**
     * Appelle les APIs externes pour vérifier URLs et numéros.
     */
    private suspend fun checkApis(urls: List<String>, senderNumber: String?): ApiCheckResults {
        var isMalicious = false
        var virusScore = 0
        var isFraudNumber = false

        // Vérification dans la base de fraude locale
        if (senderNumber != null) {
            isFraudNumber = FraudDatabase.isKnownFraudNumber(senderNumber)
        }
        urls.forEach { url ->
            if (FraudDatabase.isKnownFraudUrl(url)) {
                isMalicious = true
            }
        }

        // Google Safe Browsing
        if (urls.isNotEmpty()) {
            try {
                val apiKey = BuildConfig.GOOGLE_SAFE_BROWSING_API_KEY
                if (apiKey != "YOUR_API_KEY_HERE") {
                    val request = SafeBrowsingRequest(
                        threatInfo = ThreatInfo(
                            threatEntries = urls.map { ThreatEntry(it) }
                        )
                    )
                    val response = ApiClient.safeBrowsingService.checkUrl(apiKey, request)
                    if (!response.matches.isNullOrEmpty()) {
                        isMalicious = true
                    }
                }
            } catch (_: Exception) {
                // Silencieux — on continue avec les autres vérifications
            }
        }

        // VirusTotal
        if (urls.isNotEmpty()) {
            try {
                val apiKey = BuildConfig.VIRUSTOTAL_API_KEY
                if (apiKey != "YOUR_API_KEY_HERE") {
                    val response = ApiClient.virusTotalService.checkUrl(apiKey, urls.first())
                    val stats = response.data?.attributes?.last_analysis_stats
                    virusScore = (stats?.malicious ?: 0) + (stats?.suspicious ?: 0)
                }
            } catch (_: Exception) {
                // Silencieux
            }
        }

        return ApiCheckResults(
            isMaliciousUrl = isMalicious,
            isKnownFraudNumber = isFraudNumber,
            virusTotalScore = virusScore
        )
    }

    /**
     * Récupère les numéros de téléphone des contacts de l'utilisateur.
     */
    private fun getKnownContacts(): Set<String> {
        val contacts = mutableSetOf<String>()
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )
            cursor?.use {
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val number = it.getString(numberIndex)?.replace(" ", "")?.replace("-", "")
                    if (number != null) contacts.add(number)
                }
            }
        } catch (_: SecurityException) {
            // Permission non accordée
        }
        return contacts
    }

    // --- Accès aux données stockées ---

    fun getRecentMessages(): Flow<List<AnalyzedMessageEntity>> = dao.getRecentMessages()

    fun getLastMessage(): Flow<AnalyzedMessageEntity?> = dao.getLastMessage()

    suspend fun getMessageById(id: Long): AnalyzedMessageEntity? = dao.getById(id)

    suspend fun deleteMessage(id: Long) = dao.deleteById(id)

    /**
     * Sauvegarde directe d'un message analysé (utilisé par l'analyse email).
     */
    suspend fun saveMessage(entity: AnalyzedMessageEntity): Long = dao.insert(entity)
}
