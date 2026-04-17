package com.coachantiarnaque.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Service Retrofit pour l'API VirusTotal.
 * Vérifie la réputation d'une URL.
 */
interface VirusTotalService {

    @GET("api/v3/urls")
    suspend fun checkUrl(
        @Header("x-apikey") apiKey: String,
        @Query("url") url: String
    ): VirusTotalResponse
}

data class VirusTotalResponse(
    val data: VirusTotalData?
)

data class VirusTotalData(
    val attributes: VirusTotalAttributes?
)

data class VirusTotalAttributes(
    val last_analysis_stats: AnalysisStats?
)

data class AnalysisStats(
    val malicious: Int = 0,
    val suspicious: Int = 0,
    val harmless: Int = 0,
    val undetected: Int = 0
)
