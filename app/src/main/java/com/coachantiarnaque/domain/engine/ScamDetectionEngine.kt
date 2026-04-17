package com.coachantiarnaque.domain.engine

import com.coachantiarnaque.domain.model.AnalysisResult

/**
 * Moteur de détection d'arnaques basé sur des règles (sans IA).
 * Analyse le contenu d'un message et retourne un score + raisons.
 */
class ScamDetectionEngine {

    companion object {
        // Mots-clés suspects fréquemment utilisés dans les arnaques
        val SUSPICIOUS_KEYWORDS = listOf(
            "urgence", "immédiatement", "compte bloqué", "cliquez ici",
            "paiement", "colis", "banque", "vérifiez votre compte",
            "gagné", "félicitations", "remboursement", "carte bancaire",
            "mot de passe", "confirmer", "suspendre", "expiration",
            "livraison", "douane", "impôt", "amende", "police",
            "sécurité sociale", "cpam", "caf", "vignette crit'air",
            "renouveler", "mettre à jour", "dernière chance",
            "dans les 24h", "dans les 48h", "action requise",
            "votre colis", "frais de port", "débloquer"
        )

        // Regex pour détecter les URLs
        val URL_REGEX = Regex(
            """(https?://[^\s]+)|(www\.[^\s]+)""",
            RegexOption.IGNORE_CASE
        )

        // Raccourcisseurs d'URL suspects
        val URL_SHORTENERS = listOf(
            "bit.ly", "tinyurl", "t.co", "goo.gl", "ow.ly",
            "is.gd", "buff.ly", "adf.ly", "tiny.cc", "rb.gy",
            "cutt.ly", "shorturl.at"
        )

        // Patterns de numéros surtaxés ou suspects
        val SUSPICIOUS_NUMBER_PATTERNS = listOf(
            Regex("""^08\d{8}$"""),      // Numéros surtaxés FR
            Regex("""^\+?(?!33)\d+"""),   // Numéros internationaux (hors France)
        )
    }

    /**
     * Analyse un message et retourne le résultat complet.
     * @param content Le texte du message à analyser
     * @param senderNumber Le numéro de l'expéditeur (optionnel)
     * @param knownContacts Liste des numéros connus de l'utilisateur
     * @param apiResults Résultats additionnels des APIs externes
     */
    fun analyze(
        content: String,
        senderNumber: String? = null,
        knownContacts: Set<String> = emptySet(),
        apiResults: ApiCheckResults = ApiCheckResults()
    ): AnalysisResult {
        var score = 0
        val reasons = mutableListOf<String>()
        val lowerContent = content.lowercase()

        // 1. Vérification des mots-clés suspects
        val foundKeywords = SUSPICIOUS_KEYWORDS.filter { lowerContent.contains(it) }
        if (foundKeywords.isNotEmpty()) {
            score += foundKeywords.size // +1 par mot suspect
            reasons.add("Ce message contient des mots suspects : ${foundKeywords.joinToString(", ")}")
        }

        // 2. Détection de liens
        val urls = URL_REGEX.findAll(content).map { it.value }.toList()
        if (urls.isNotEmpty()) {
            score += 2 // +2 pour présence de lien
            reasons.add("Ce message contient un lien — soyez prudent avant de cliquer")

            // 3. Vérification raccourcisseurs d'URL
            val shorteners = urls.filter { url ->
                URL_SHORTENERS.any { shortener -> url.lowercase().contains(shortener) }
            }
            if (shorteners.isNotEmpty()) {
                score += 3 // +3 pour raccourcisseur
                reasons.add("Ce message utilise un lien raccourci qui cache la vraie adresse")
            }
        }

        // 4. Numéro inconnu
        if (senderNumber != null && senderNumber !in knownContacts) {
            score += 1
            reasons.add("Ce message vient d'un numéro qui n'est pas dans vos contacts, mais le numéro ne semble pas suspect")
        }

        // 5. Détection de sentiment d'urgence
        val urgencyPatterns = listOf(
            "immédiatement", "urgent", "dans les 24h", "dans les 48h",
            "dernière chance", "action requise", "sans délai", "tout de suite"
        )
        if (urgencyPatterns.any { lowerContent.contains(it) }) {
            score += 1
            reasons.add("Ce message crée un sentiment d'urgence pour vous pousser à agir vite")
        }

        // 6. Demande d'informations personnelles
        val personalInfoPatterns = listOf(
            "mot de passe", "numéro de carte", "code secret",
            "identifiant", "coordonnées bancaires", "rib", "iban"
        )
        if (personalInfoPatterns.any { lowerContent.contains(it) }) {
            score += 2
            reasons.add("Ce message demande des informations personnelles ou bancaires")
        }

        // 7. Résultats API externes
        if (apiResults.isMaliciousUrl) {
            score += 4
            reasons.add("Le lien dans ce message a été identifié comme dangereux")
        }
        if (apiResults.isKnownFraudNumber) {
            score += 3
            reasons.add("Ce numéro est répertorié comme frauduleux")
        }
        if (apiResults.virusTotalScore > 3) {
            score += 3
            reasons.add("Le lien a une mauvaise réputation sur internet")
        }

        // Si aucune raison détectée
        if (reasons.isEmpty()) {
            reasons.add("Aucun élément suspect détecté dans ce message")
        }

        return AnalysisResult.fromScore(score, reasons)
    }
}

/**
 * Résultats des vérifications via APIs externes.
 */
data class ApiCheckResults(
    val isMaliciousUrl: Boolean = false,
    val isKnownFraudNumber: Boolean = false,
    val virusTotalScore: Int = 0
)
