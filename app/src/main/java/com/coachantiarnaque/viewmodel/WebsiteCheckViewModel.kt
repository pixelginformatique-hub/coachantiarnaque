package com.coachantiarnaque.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.coachantiarnaque.BuildConfig
import com.coachantiarnaque.data.api.*
import com.coachantiarnaque.domain.engine.WebsiteAnalysisEngine
import com.coachantiarnaque.domain.engine.WebsiteAnalysisResult
import com.coachantiarnaque.domain.engine.WebsiteApiResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * ViewModel pour l'écran de vérification de site web.
 */
class WebsiteCheckViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = WebsiteAnalysisEngine()

    private val _result = MutableStateFlow<WebsiteAnalysisResult?>(null)
    val result: StateFlow<WebsiteAnalysisResult?> = _result.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _analyzedUrl = MutableStateFlow("")
    val analyzedUrl: StateFlow<String> = _analyzedUrl.asStateFlow()

    /**
     * Lance l'analyse complète d'un site web.
     */
    fun analyzeWebsite(url: String) {
        if (url.isBlank()) return

        if (!engine.isValidUrl(url)) {
            _error.value = "Le lien semble incorrect. Vérifiez l'adresse et réessayez."
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _error.value = null
            _result.value = null
            _analyzedUrl.value = url

            try {
                val apiResults = withContext(Dispatchers.IO) {
                    fetchApiResults(url)
                }
                val analysisResult = engine.analyze(url, apiResults)
                _result.value = analysisResult
            } catch (e: Exception) {
                // En cas d'erreur réseau, analyser quand même avec les règles locales
                try {
                    val localResult = engine.analyze(url)
                    _result.value = localResult
                } catch (_: Exception) {
                    _error.value = "Impossible de vérifier pour le moment. Vérifiez votre connexion."
                }
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Appelle les APIs externes pour vérifier le site.
     */
    private suspend fun fetchApiResults(url: String): WebsiteApiResults {
        val domain = engine.extractDomain(url) ?: url
        val fullUrl = if (!url.startsWith("http")) "https://$url" else url

        var isMalicious = false
        var vtMalicious = 0
        var domainAgeDays = -1

        // Vérification base de fraude locale
        if (FraudDatabase.isKnownFraudUrl(fullUrl) || FraudDatabase.isKnownFraudUrl(domain)) {
            isMalicious = true
        }

        // Google Safe Browsing
        try {
            val apiKey = BuildConfig.GOOGLE_SAFE_BROWSING_API_KEY
            if (apiKey != "YOUR_API_KEY_HERE") {
                val request = SafeBrowsingRequest(
                    threatInfo = ThreatInfo(
                        threatEntries = listOf(ThreatEntry(fullUrl))
                    )
                )
                val response = ApiClient.safeBrowsingService.checkUrl(apiKey, request)
                if (!response.matches.isNullOrEmpty()) {
                    isMalicious = true
                }
            }
        } catch (_: Exception) { }

        // VirusTotal
        try {
            val apiKey = BuildConfig.VIRUSTOTAL_API_KEY
            if (apiKey != "YOUR_API_KEY_HERE") {
                val response = ApiClient.virusTotalService.checkUrl(apiKey, fullUrl)
                val stats = response.data?.attributes?.last_analysis_stats
                vtMalicious = (stats?.malicious ?: 0) + (stats?.suspicious ?: 0)
            }
        } catch (_: Exception) { }

        // WHOIS - Âge du domaine
        try {
            val apiKey = BuildConfig.WHOIS_API_KEY
            if (apiKey != "YOUR_API_KEY_HERE") {
                val response = ApiClient.whoisApiService.lookup(apiKey, domain)
                val createdDate = response.WhoisRecord?.createdDate
                    ?: response.WhoisRecord?.registryData?.createdDate
                if (createdDate != null) {
                    domainAgeDays = parseDomainAge(createdDate)
                }
            }
        } catch (_: Exception) { }

        return WebsiteApiResults(
            isMalicious = isMalicious,
            virusTotalMalicious = vtMalicious,
            domainAgeDays = domainAgeDays
        )
    }

    /**
     * Parse une date de création de domaine et retourne l'âge en jours.
     */
    private fun parseDomainAge(dateStr: String): Int {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                val created = sdf.parse(dateStr) ?: continue
                val now = Date()
                val diffMs = now.time - created.time
                return TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
            } catch (_: Exception) { }
        }
        return -1
    }

    fun clearResult() {
        _result.value = null
        _error.value = null
    }
}
