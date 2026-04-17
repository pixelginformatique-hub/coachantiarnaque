package com.coachantiarnaque.data.api

/**
 * Base de données locale simulant les sources de fraude :
 * - Canadian Anti-Fraud Centre
 * - FTC
 * - Signal Spam
 *
 * En production, ces données seraient récupérées via API.
 */
object FraudDatabase {

    // Numéros frauduleux connus (simulés)
    val KNOWN_FRAUD_NUMBERS = setOf(
        "+33644000000", "+33755000000", "+33600000000",
        "+33612345678", "+33698765432", "+33700000000",
        "+14165550100", "+18005551234", "+447911123456"
    )

    // URLs frauduleuses connues (simulées)
    val KNOWN_FRAUD_URLS = setOf(
        "secure-banque-fr.com",
        "colis-livraison-suivi.com",
        "amende-gouv-paiement.com",
        "cpam-remboursement.net",
        "carte-vitale-renouvellement.fr",
        "impots-remboursement.com",
        "caf-allocation-mise-a-jour.fr",
        "chronopost-colis-suivi.com",
        "la-poste-colis-retenu.com",
        "netflix-paiement-echec.com"
    )

    /**
     * Vérifie si un numéro est dans la base de fraude.
     */
    fun isKnownFraudNumber(number: String): Boolean {
        val cleaned = number.replace(" ", "").replace("-", "")
        return KNOWN_FRAUD_NUMBERS.any { cleaned.contains(it) || it.contains(cleaned) }
    }

    /**
     * Vérifie si une URL est dans la base de fraude.
     */
    fun isKnownFraudUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return KNOWN_FRAUD_URLS.any { lowerUrl.contains(it) }
    }
}
