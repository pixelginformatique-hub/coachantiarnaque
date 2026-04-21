package com.coachantiarnaque.domain.engine

import com.coachantiarnaque.R
import com.coachantiarnaque.domain.model.AnalysisResult
import com.coachantiarnaque.utils.StringProvider

/**
 * Moteur de détection d'arnaques SMS basé sur des règles (sans IA).
 */
class ScamDetectionEngine {

    companion object {
        val SUSPICIOUS_KEYWORDS = listOf(
            "urgence", "immédiatement", "compte bloqué", "cliquez ici",
            "paiement", "colis", "banque", "vérifiez votre compte",
            "gagné", "félicitations", "remboursement", "carte bancaire",
            "mot de passe", "confirmer", "suspendre", "expiration",
            "livraison", "douane", "impôt", "amende", "police",
            "sécurité sociale", "cpam", "caf", "vignette crit'air",
            "renouveler", "mettre à jour", "dernière chance",
            "dans les 24h", "dans les 48h", "action requise",
            "votre colis", "frais de port", "débloquer",
            "urgent", "immediately", "account blocked", "click here",
            "payment", "parcel", "verify your account", "won",
            "congratulations", "refund", "credit card", "password",
            "confirm", "suspended", "expiring", "delivery",
            "customs", "tax", "fine", "renew", "update",
            "last chance", "within 24h", "within 48h", "action required",
            "your package", "shipping fee", "unlock"
        )

        val URL_REGEX = Regex("""(https?://[^\s]+)|(www\.[^\s]+)""", RegexOption.IGNORE_CASE)

        val URL_SHORTENERS = listOf(
            "bit.ly", "tinyurl", "t.co", "goo.gl", "ow.ly",
            "is.gd", "buff.ly", "adf.ly", "tiny.cc", "rb.gy",
            "cutt.ly", "shorturl.at"
        )
    }

    fun analyze(
        content: String,
        senderNumber: String? = null,
        knownContacts: Set<String> = emptySet(),
        apiResults: ApiCheckResults = ApiCheckResults(),
        sp: StringProvider? = null
    ): AnalysisResult {
        var score = 0
        val reasons = mutableListOf<String>()
        val lowerContent = content.lowercase()

        val foundKeywords = SUSPICIOUS_KEYWORDS.filter { lowerContent.contains(it) }
        if (foundKeywords.isNotEmpty()) {
            score += foundKeywords.size
            reasons.add(sp?.getString(R.string.reason_suspicious_words, foundKeywords.joinToString(", "))
                ?: "Ce message contient des mots suspects : ${foundKeywords.joinToString(", ")}")
        }

        val urls = URL_REGEX.findAll(content).map { it.value }.toList()
        if (urls.isNotEmpty()) {
            score += 2
            reasons.add(sp?.getString(R.string.reason_link_detected)
                ?: "Ce message contient un lien — soyez prudent avant de cliquer")

            val shorteners = urls.filter { url -> URL_SHORTENERS.any { s -> url.lowercase().contains(s) } }
            if (shorteners.isNotEmpty()) {
                score += 3
                reasons.add(sp?.getString(R.string.reason_shortener)
                    ?: "Ce message utilise un lien raccourci qui cache la vraie adresse")
            }
        }

        if (senderNumber != null && senderNumber !in knownContacts) {
            score += 1
            reasons.add(sp?.getString(R.string.reason_unknown_number)
                ?: "Ce message vient d'un numéro qui n'est pas dans vos contacts, mais le numéro ne semble pas suspect")
        }

        val urgencyPatterns = listOf(
            "immédiatement", "urgent", "dans les 24h", "dans les 48h",
            "dernière chance", "action requise", "sans délai", "tout de suite",
            "immediately", "within 24 hours", "within 48 hours",
            "last chance", "action required", "right away", "act now"
        )
        if (urgencyPatterns.any { lowerContent.contains(it) }) {
            score += 1
            reasons.add(sp?.getString(R.string.reason_urgency)
                ?: "Ce message crée un sentiment d'urgence pour vous pousser à agir vite")
        }

        val personalInfoPatterns = listOf(
            "mot de passe", "numéro de carte", "code secret",
            "identifiant", "coordonnées bancaires", "rib", "iban",
            "password", "card number", "secret code",
            "login", "bank details", "social security number"
        )
        if (personalInfoPatterns.any { lowerContent.contains(it) }) {
            score += 2
            reasons.add(sp?.getString(R.string.reason_personal_info)
                ?: "Ce message demande des informations personnelles ou bancaires")
        }

        if (apiResults.isMaliciousUrl) {
            score += 4
            reasons.add(sp?.getString(R.string.reason_malicious_url)
                ?: "Le lien dans ce message a été identifié comme dangereux")
        }
        if (apiResults.isKnownFraudNumber) {
            score += 3
            reasons.add(sp?.getString(R.string.reason_fraud_number)
                ?: "Ce numéro est répertorié comme frauduleux")
        }
        if (apiResults.virusTotalScore > 3) {
            score += 3
            reasons.add(sp?.getString(R.string.reason_bad_reputation)
                ?: "Le lien a une mauvaise réputation sur internet")
        }

        if (reasons.isEmpty()) {
            reasons.add(sp?.getString(R.string.reason_none)
                ?: "Aucun élément suspect détecté dans ce message")
        }

        return AnalysisResult.fromScore(score, reasons)
    }
}

data class ApiCheckResults(
    val isMaliciousUrl: Boolean = false,
    val isKnownFraudNumber: Boolean = false,
    val virusTotalScore: Int = 0
)
