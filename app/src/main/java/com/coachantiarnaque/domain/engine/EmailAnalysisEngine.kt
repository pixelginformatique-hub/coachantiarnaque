package com.coachantiarnaque.domain.engine

/**
 * Niveau de risque d'un email.
 * Wording légal : "signes de risque", jamais "arnaque".
 */
enum class EmailRiskLevel {
    LOW,      // 🟢 Faible risque
    MODERATE, // 🟠 Risque modéré
    HIGH      // 🔴 Risque élevé
}

/**
 * Résultat de l'analyse d'un email.
 */
data class EmailAnalysisResult(
    val score: Int,
    val riskLevel: EmailRiskLevel,
    val reasons: List<String>
) {
    companion object {
        fun fromScore(score: Int, reasons: List<String>): EmailAnalysisResult {
            val level = when {
                score >= 6 -> EmailRiskLevel.HIGH
                score >= 3 -> EmailRiskLevel.MODERATE
                else -> EmailRiskLevel.LOW
            }
            return EmailAnalysisResult(score, level, reasons)
        }
    }
}

/**
 * Moteur d'analyse d'emails basé sur des règles (sans IA).
 * Analyse le contenu et l'adresse de l'expéditeur.
 */
class EmailAnalysisEngine {

    companion object {
        // Mots-clés suspects (réutilisés du moteur SMS + spécifiques email)
        val SUSPICIOUS_KEYWORDS = listOf(
            "urgence", "immédiatement", "compte bloqué", "cliquez ici",
            "paiement", "banque", "vérifiez votre compte",
            "mot de passe", "confirmer", "suspendre", "expiration",
            "remboursement", "carte bancaire", "coordonnées bancaires",
            "gagné", "félicitations", "dernière chance",
            "action requise", "dans les 24h", "dans les 48h",
            "votre compte sera", "désactivé", "fermé",
            "mettre à jour vos informations", "connexion inhabituelle",
            "activité suspecte", "sécuriser votre compte",
            "facture", "impayé", "relance", "huissier",
            "héritage", "loterie", "million", "bénéficiaire"
        )

        val URL_REGEX = Regex(
            """(https?://[^\s]+)|(www\.[^\s]+)""",
            RegexOption.IGNORE_CASE
        )

        val URL_SHORTENERS = listOf(
            "bit.ly", "tinyurl", "t.co", "goo.gl", "ow.ly",
            "is.gd", "buff.ly", "adf.ly", "tiny.cc", "rb.gy",
            "cutt.ly", "shorturl.at"
        )

        // Domaines gratuits
        val FREE_EMAIL_DOMAINS = listOf(
            "gmail.com", "yahoo.com", "yahoo.fr", "outlook.com",
            "hotmail.com", "hotmail.fr", "live.com", "aol.com",
            "protonmail.com", "mail.com", "yandex.com", "gmx.com"
        )

        // Marques souvent usurpées
        val IMPERSONATED_BRANDS = listOf(
            "banque", "paypal", "amazon", "apple", "microsoft",
            "netflix", "google", "facebook", "instagram", "desjardins",
            "rbc", "td", "bmo", "poste", "chronopost", "colissimo",
            "impot", "gouv", "caf", "cpam", "ameli", "edf"
        )

        // Typosquatting connu
        val TYPOSQUAT_PATTERNS = listOf(
            "amaz0n", "paypa1", "g00gle", "faceb00k", "micros0ft",
            "app1e", "netf1ix", "go0gle", "amazom", "paypai",
            "arnazon", "arnazon", "rnicrosoft", "gooogle", "yahooo",
            "banqu3", "desjard1ns"
        )

        // TLDs suspects
        val SUSPICIOUS_TLDS = listOf(
            ".xyz", ".top", ".club", ".work", ".buzz", ".icu",
            ".site", ".online", ".fun", ".space", ".click",
            ".link", ".tk", ".ml", ".ga", ".cf", ".gq"
        )

        val EMAIL_REGEX = Regex(
            """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"""
        )
    }

    /**
     * Analyse complète d'un email : contenu + expéditeur.
     */
    fun analyze(
        content: String,
        senderEmail: String? = null
    ): EmailAnalysisResult {
        var score = 0
        val reasons = mutableListOf<String>()
        val lowerContent = content.lowercase()

        // === ANALYSE DU CONTENU ===

        // 1. Mots-clés suspects
        val foundKeywords = SUSPICIOUS_KEYWORDS.filter { lowerContent.contains(it) }
        if (foundKeywords.isNotEmpty()) {
            score += foundKeywords.size.coerceAtMost(3) // max +3
            reasons.add("Ce message contient des expressions souvent utilisées dans les emails à risque")
        }

        // 2. Détection de liens
        val urls = URL_REGEX.findAll(content).map { it.value }.toList()
        if (urls.isNotEmpty()) {
            score += 2
            reasons.add("Cet email contient ${urls.size} lien(s) — vérifiez avant de cliquer")

            // 3. Raccourcisseurs d'URL
            val shorteners = urls.filter { url ->
                URL_SHORTENERS.any { s -> url.lowercase().contains(s) }
            }
            if (shorteners.isNotEmpty()) {
                score += 3
                reasons.add("Un lien utilise un raccourcisseur qui cache la vraie adresse")
            }
        }

        // 4. Sentiment d'urgence
        val urgencyPatterns = listOf(
            "immédiatement", "urgent", "dans les 24h", "dans les 48h",
            "dernière chance", "action requise", "sans délai",
            "votre compte sera fermé", "votre compte sera désactivé"
        )
        if (urgencyPatterns.any { lowerContent.contains(it) }) {
            score += 1
            reasons.add("Ce message crée un sentiment d'urgence pour vous pousser à agir vite")
        }

        // 5. Demande d'informations personnelles
        val personalInfoPatterns = listOf(
            "mot de passe", "numéro de carte", "code secret",
            "identifiant", "coordonnées bancaires", "rib", "iban",
            "numéro de sécurité sociale", "date de naissance"
        )
        if (personalInfoPatterns.any { lowerContent.contains(it) }) {
            score += 2
            reasons.add("Ce message demande des informations personnelles ou bancaires")
        }

        // === ANALYSE DE L'EXPÉDITEUR ===
        if (!senderEmail.isNullOrBlank()) {
            val senderReasons = analyzeSender(senderEmail, lowerContent)
            score += senderReasons.first
            reasons.addAll(senderReasons.second)
        }

        // Si aucune raison
        if (reasons.isEmpty()) {
            reasons.add("Aucun signe de risque particulier détecté dans cet email")
        }

        return EmailAnalysisResult.fromScore(score, reasons)
    }

    /**
     * Analyse l'adresse email de l'expéditeur.
     * @return Pair(score, raisons)
     */
    private fun analyzeSender(email: String, contentLower: String): Pair<Int, List<String>> {
        var score = 0
        val reasons = mutableListOf<String>()
        val lowerEmail = email.lowercase().trim()

        // Extraire le domaine
        val domain = lowerEmail.substringAfter("@", "")
        if (domain.isEmpty()) return Pair(0, emptyList())

        // 1. Typosquatting
        val typo = TYPOSQUAT_PATTERNS.firstOrNull { domain.contains(it) }
        if (typo != null) {
            score += 2
            reasons.add("L'adresse de l'expéditeur ressemble à une imitation ($typo)")
        }

        // 2. Domaine gratuit + contenu mentionne une marque/institution
        val isFreeEmail = FREE_EMAIL_DOMAINS.any { domain == it }
        if (isFreeEmail) {
            val brandMentioned = IMPERSONATED_BRANDS.firstOrNull { contentLower.contains(it) }
            if (brandMentioned != null) {
                score += 2
                reasons.add("L'email provient d'une adresse gratuite mais mentionne \"$brandMentioned\" — les vrais organismes utilisent leur propre domaine")
            }
        }

        // 3. TLD suspect
        val suspiciousTld = SUSPICIOUS_TLDS.firstOrNull { domain.endsWith(it) }
        if (suspiciousTld != null) {
            score += 1
            reasons.add("L'adresse de l'expéditeur utilise une extension inhabituelle ($suspiciousTld)")
        }

        // 4. Domaine très long
        if (domain.length > 25) {
            score += 1
            reasons.add("Le domaine de l'expéditeur est inhabituellement long")
        }

        // 5. Beaucoup de chiffres dans le domaine
        val digitCount = domain.count { it.isDigit() }
        if (digitCount > 3) {
            score += 1
            reasons.add("L'adresse de l'expéditeur contient beaucoup de chiffres")
        }

        return Pair(score, reasons)
    }

    /**
     * Vérifie si une adresse email a un format valide.
     */
    fun isValidEmail(email: String): Boolean {
        return email.isBlank() || EMAIL_REGEX.matches(email.trim())
    }
}
