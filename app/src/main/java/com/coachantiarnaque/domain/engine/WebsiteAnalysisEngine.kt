package com.coachantiarnaque.domain.engine

import java.net.URI

/**
 * Niveau de risque d'un site web.
 * Wording légal : on parle de "signes de risque", jamais d'arnaque.
 */
enum class RiskLevel {
    LOW,      // 🟢 Faible risque
    MODERATE, // 🟠 Risque modéré
    HIGH      // 🔴 Risque élevé
}

/**
 * Résultat de l'analyse d'un site web.
 */
data class WebsiteAnalysisResult(
    val score: Int,
    val riskLevel: RiskLevel,
    val reasons: List<String>,
    val domain: String
) {
    companion object {
        fun fromScore(score: Int, reasons: List<String>, domain: String): WebsiteAnalysisResult {
            val level = when {
                score >= 6 -> RiskLevel.HIGH
                score >= 3 -> RiskLevel.MODERATE
                else -> RiskLevel.LOW
            }
            return WebsiteAnalysisResult(score, level, reasons, domain)
        }
    }
}

/**
 * Moteur d'analyse de sites web basé sur des règles (sans IA).
 * Analyse URL, nom de domaine, TLD, et intègre les résultats d'APIs externes.
 */
class WebsiteAnalysisEngine {

    companion object {
        // TLDs considérés comme inhabituels / à risque
        val SUSPICIOUS_TLDS = listOf(
            ".xyz", ".top", ".club", ".work", ".buzz", ".icu",
            ".site", ".online", ".fun", ".space", ".click",
            ".link", ".info", ".tk", ".ml", ".ga", ".cf", ".gq",
            ".pw", ".cc", ".ws", ".bid", ".stream", ".racing"
        )

        // Mots commerciaux souvent utilisés dans les faux sites
        val BRAND_KEYWORDS = listOf(
            "nike", "adidas", "amazon", "apple", "google", "facebook",
            "instagram", "paypal", "netflix", "microsoft", "samsung",
            "louis-vuitton", "gucci", "chanel", "hermes", "dior",
            "walmart", "costco", "bestbuy", "ebay", "aliexpress",
            "banque", "desjardins", "rbc", "td", "bmo", "scotiabank"
        )

        // Mots suspects dans les domaines
        val SUSPICIOUS_DOMAIN_WORDS = listOf(
            "cheap", "deal", "free", "promo", "discount", "sale",
            "login", "secure", "verify", "update", "account",
            "officiel", "official", "support", "service", "help",
            "gratuit", "cadeau", "gagnant", "winner", "prize"
        )

        val URL_REGEX = Regex(
            """^(https?://)?([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(/.*)?$""",
            RegexOption.IGNORE_CASE
        )
    }

    /**
     * Valide et normalise une URL.
     * @return le domaine extrait, ou null si invalide.
     */
    fun extractDomain(input: String): String? {
        val url = if (!input.startsWith("http")) "https://$input" else input
        return try {
            val uri = URI(url)
            val host = uri.host ?: return null
            host.removePrefix("www.")
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Vérifie si l'URL a un format valide.
     */
    fun isValidUrl(input: String): Boolean {
        val url = if (!input.startsWith("http")) "https://$input" else input
        return try {
            val uri = URI(url)
            uri.host != null && uri.host!!.contains(".")
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Analyse complète d'un site web.
     * @param url L'URL à analyser
     * @param apiResults Résultats des vérifications API externes
     */
    fun analyze(
        url: String,
        apiResults: WebsiteApiResults = WebsiteApiResults()
    ): WebsiteAnalysisResult {
        var score = 0
        val reasons = mutableListOf<String>()
        val domain = extractDomain(url) ?: url

        // 1. TLD suspect
        val tld = SUSPICIOUS_TLDS.firstOrNull { domain.endsWith(it) }
        if (tld != null) {
            score += 1
            reasons.add("L'extension du site ($tld) est inhabituelle")
        }

        // 2. Nom de domaine contient un mot de marque connue
        val brandFound = BRAND_KEYWORDS.firstOrNull { domain.lowercase().contains(it) }
        if (brandFound != null) {
            // Vérifier que ce n'est pas le vrai domaine de la marque
            val isLikelyFake = !domain.equals("$brandFound.com", ignoreCase = true) &&
                !domain.equals("$brandFound.ca", ignoreCase = true) &&
                !domain.equals("$brandFound.fr", ignoreCase = true) &&
                !domain.endsWith(".$brandFound.com", ignoreCase = true)
            if (isLikelyFake) {
                score += 2
                reasons.add("Le nom du site utilise une marque connue ($brandFound) de façon inhabituelle")
            }
        }

        // 3. Mots suspects dans le domaine
        val suspiciousWords = SUSPICIOUS_DOMAIN_WORDS.filter { domain.lowercase().contains(it) }
        if (suspiciousWords.isNotEmpty()) {
            score += 1
            reasons.add("Le nom du site contient des mots souvent associés à des sites à risque")
        }

        // 4. Domaine très long (souvent signe de phishing)
        if (domain.length > 30) {
            score += 1
            reasons.add("Le nom du site est inhabituellement long")
        }

        // 5. Beaucoup de tirets dans le domaine
        val dashCount = domain.count { it == '-' }
        if (dashCount >= 2) {
            score += 1
            reasons.add("Le nom du site contient plusieurs tirets, ce qui est inhabituel")
        }

        // 6. Sous-domaines multiples
        val parts = domain.split(".")
        if (parts.size > 3) {
            score += 1
            reasons.add("Le site utilise plusieurs sous-domaines, ce qui peut être suspect")
        }

        // 7. Résultats API : Google Safe Browsing
        if (apiResults.isMalicious) {
            score += 3
            reasons.add("Ce lien est signalé comme potentiellement dangereux par les bases de sécurité")
        }

        // 8. Résultats API : VirusTotal
        if (apiResults.virusTotalMalicious > 3) {
            score += 3
            reasons.add("Plusieurs services de sécurité signalent ce site comme à risque")
        } else if (apiResults.virusTotalMalicious > 0) {
            score += 1
            reasons.add("Certains services de sécurité ont émis des alertes sur ce site")
        }

        // 9. Âge du domaine
        when {
            apiResults.domainAgeDays in 0..6 -> {
                score += 3
                reasons.add("Le site est très récent (moins d'une semaine)")
            }
            apiResults.domainAgeDays in 7..29 -> {
                score += 2
                reasons.add("Le site est récent (moins d'un mois)")
            }
            apiResults.domainAgeDays in 30..89 -> {
                score += 1
                reasons.add("Le site a été créé il y a moins de 3 mois")
            }
            apiResults.domainAgeDays >= 90 -> {
                // Pas de pénalité, mais on peut noter positivement
            }
            // -1 = inconnu, pas de pénalité
        }

        // Si aucune raison détectée
        if (reasons.isEmpty()) {
            reasons.add("Aucun signe de risque particulier détecté pour ce site")
        }

        return WebsiteAnalysisResult.fromScore(score, reasons, domain)
    }
}

/**
 * Résultats des vérifications API externes pour un site web.
 */
data class WebsiteApiResults(
    val isMalicious: Boolean = false,
    val virusTotalMalicious: Int = 0,
    val domainAgeDays: Int = -1 // -1 = inconnu
)
