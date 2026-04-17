package com.coachantiarnaque.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service Retrofit pour l'API WhoisXML.
 * Récupère les informations d'enregistrement d'un domaine.
 */
interface WhoisApiService {

    @GET("whoisserver/WhoisService")
    suspend fun lookup(
        @Query("apiKey") apiKey: String,
        @Query("domainName") domain: String,
        @Query("outputFormat") format: String = "JSON"
    ): WhoisResponse
}

data class WhoisResponse(
    val WhoisRecord: WhoisRecord?
)

data class WhoisRecord(
    val createdDate: String?,
    val registryData: RegistryData?
)

data class RegistryData(
    val createdDate: String?
)
