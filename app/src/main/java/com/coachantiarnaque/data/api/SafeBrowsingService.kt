package com.coachantiarnaque.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Service Retrofit pour l'API Google Safe Browsing.
 * Vérifie si une URL est répertoriée comme malveillante.
 */
interface SafeBrowsingService {

    @POST("v4/threatMatches:find")
    suspend fun checkUrl(
        @Query("key") apiKey: String,
        @Body request: SafeBrowsingRequest
    ): SafeBrowsingResponse
}

// --- Modèles de requête ---

data class SafeBrowsingRequest(
    val client: ClientInfo = ClientInfo(),
    val threatInfo: ThreatInfo
)

data class ClientInfo(
    val clientId: String = "coach-anti-arnaque",
    val clientVersion: String = "1.0.0"
)

data class ThreatInfo(
    val threatTypes: List<String> = listOf(
        "MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"
    ),
    val platformTypes: List<String> = listOf("ANY_PLATFORM"),
    val threatEntryTypes: List<String> = listOf("URL"),
    val threatEntries: List<ThreatEntry>
)

data class ThreatEntry(val url: String)

// --- Modèles de réponse ---

data class SafeBrowsingResponse(
    val matches: List<ThreatMatch>?
)

data class ThreatMatch(
    val threatType: String?,
    val platformType: String?,
    val threat: ThreatEntry?
)
