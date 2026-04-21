package com.coachantiarnaque.domain.engine

import com.coachantiarnaque.R
import com.coachantiarnaque.utils.StringProvider

enum class EmailRiskLevel { LOW, MODERATE, HIGH }

data class EmailAnalysisResult(val score: Int, val riskLevel: EmailRiskLevel, val reasons: List<String>) {
    companion object {
        fun fromScore(score: Int, reasons: List<String>): EmailAnalysisResult {
            val level = when { score >= 6 -> EmailRiskLevel.HIGH; score >= 3 -> EmailRiskLevel.MODERATE; else -> EmailRiskLevel.LOW }
            return EmailAnalysisResult(score, level, reasons)
        }
    }
}

class EmailAnalysisEngine {
    companion object {
        val SUSPICIOUS_KEYWORDS = listOf(
            "urgence","immédiatement","compte bloqué","cliquez ici","paiement","banque","vérifiez votre compte",
            "mot de passe","confirmer","suspendre","expiration","remboursement","carte bancaire","coordonnées bancaires",
            "gagné","félicitations","dernière chance","action requise","dans les 24h","dans les 48h",
            "votre compte sera","désactivé","fermé","mettre à jour vos informations","connexion inhabituelle",
            "activité suspecte","sécuriser votre compte","facture","impayé","relance","huissier",
            "héritage","loterie","million","bénéficiaire",
            "urgent","immediately","account blocked","click here","payment","bank","verify your account",
            "password","confirm","suspended","expiring","refund","credit card","bank details",
            "won","congratulations","last chance","action required","within 24h","within 48h",
            "your account will be","deactivated","closed","update your information","unusual login",
            "suspicious activity","secure your account","invoice","unpaid","reminder","bailiff",
            "inheritance","lottery","beneficiary"
        )
        val URL_REGEX = Regex("""(https?://[^\s]+)|(www\.[^\s]+)""", RegexOption.IGNORE_CASE)
        val URL_SHORTENERS = listOf("bit.ly","tinyurl","t.co","goo.gl","ow.ly","is.gd","buff.ly","adf.ly","tiny.cc","rb.gy","cutt.ly","shorturl.at")
        val FREE_EMAIL_DOMAINS = listOf("gmail.com","yahoo.com","yahoo.fr","outlook.com","hotmail.com","hotmail.fr","live.com","aol.com","protonmail.com","mail.com","yandex.com","gmx.com")
        val IMPERSONATED_BRANDS = listOf("banque","paypal","amazon","apple","microsoft","netflix","google","facebook","instagram","desjardins","rbc","td","bmo","poste","chronopost","colissimo","impot","gouv","caf","cpam","ameli","edf","bank","irs","usps","fedex","ups","walmart","costco","target","wells fargo","chase","citibank")
        val TYPOSQUAT_PATTERNS = listOf("amaz0n","paypa1","g00gle","faceb00k","micros0ft","app1e","netf1ix","go0gle","amazom","paypai","arnazon","rnicrosoft","gooogle","yahooo","banqu3","desjard1ns")
        val SUSPICIOUS_TLDS = listOf(".xyz",".top",".club",".work",".buzz",".icu",".site",".online",".fun",".space",".click",".link",".tk",".ml",".ga",".cf",".gq")
        val EMAIL_REGEX = Regex("""^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""")
    }

    fun analyze(content: String, senderEmail: String? = null, sp: StringProvider? = null): EmailAnalysisResult {
        var score = 0; val reasons = mutableListOf<String>(); val lc = content.lowercase()

        val found = SUSPICIOUS_KEYWORDS.filter { lc.contains(it) }
        if (found.isNotEmpty()) { score += found.size.coerceAtMost(3); reasons.add(sp?.getString(R.string.reason_email_suspicious_words) ?: "Ce message contient des expressions souvent utilisées dans les e-mails à risque") }

        val urls = URL_REGEX.findAll(content).map { it.value }.toList()
        if (urls.isNotEmpty()) {
            score += 2; reasons.add(sp?.getString(R.string.reason_email_links, urls.size) ?: "Cet e-mail contient ${urls.size} lien(s) — vérifiez avant de cliquer")
            if (urls.any { url -> URL_SHORTENERS.any { s -> url.lowercase().contains(s) } }) { score += 3; reasons.add(sp?.getString(R.string.reason_email_shortener) ?: "Un lien utilise un raccourcisseur qui cache la vraie adresse") }
        }

        val urgency = listOf("immédiatement","urgent","dans les 24h","dans les 48h","dernière chance","action requise","sans délai","votre compte sera fermé","votre compte sera désactivé","immediately","within 24 hours","within 48 hours","last chance","action required","act now","your account will be closed")
        if (urgency.any { lc.contains(it) }) { score += 1; reasons.add(sp?.getString(R.string.reason_email_urgency) ?: "Ce message crée un sentiment d'urgence pour vous pousser à agir vite") }

        val personal = listOf("mot de passe","numéro de carte","code secret","identifiant","coordonnées bancaires","rib","iban","numéro de sécurité sociale","date de naissance","password","card number","secret code","login","bank details","social security number")
        if (personal.any { lc.contains(it) }) { score += 2; reasons.add(sp?.getString(R.string.reason_email_personal_info) ?: "Ce message demande des informations personnelles ou bancaires") }

        if (!senderEmail.isNullOrBlank()) {
            val sr = analyzeSender(senderEmail, lc, sp); score += sr.first; reasons.addAll(sr.second)
        }

        if (reasons.isEmpty()) { reasons.add(sp?.getString(R.string.reason_email_none) ?: "Aucun signe de risque particulier détecté dans cet e-mail") }
        return EmailAnalysisResult.fromScore(score, reasons)
    }

    private fun analyzeSender(email: String, contentLower: String, sp: StringProvider?): Pair<Int, List<String>> {
        var score = 0; val reasons = mutableListOf<String>(); val domain = email.lowercase().trim().substringAfter("@", "")
        if (domain.isEmpty()) return Pair(0, emptyList())

        val typo = TYPOSQUAT_PATTERNS.firstOrNull { domain.contains(it) }
        if (typo != null) { score += 2; reasons.add(sp?.getString(R.string.reason_email_typosquat, typo) ?: "L'adresse de l'expéditeur ressemble à une imitation ($typo)") }

        val isFree = FREE_EMAIL_DOMAINS.any { domain == it }
        if (isFree) {
            val brand = IMPERSONATED_BRANDS.firstOrNull { contentLower.contains(it) }
            if (brand != null) { score += 2; reasons.add(sp?.getString(R.string.reason_email_free_domain, brand) ?: "L'e-mail provient d'une adresse gratuite mais mentionne \"$brand\"") }
        }

        val suspTld = SUSPICIOUS_TLDS.firstOrNull { domain.endsWith(it) }
        if (suspTld != null) { score += 1; reasons.add(sp?.getString(R.string.reason_email_suspicious_tld, suspTld) ?: "L'adresse de l'expéditeur utilise une extension inhabituelle ($suspTld)") }
        if (domain.length > 25) { score += 1; reasons.add(sp?.getString(R.string.reason_email_long_domain) ?: "Le domaine de l'expéditeur est inhabituellement long") }
        if (domain.count { it.isDigit() } > 3) { score += 1; reasons.add(sp?.getString(R.string.reason_email_many_digits) ?: "L'adresse de l'expéditeur contient beaucoup de chiffres") }

        return Pair(score, reasons)
    }

    fun isValidEmail(email: String): Boolean = email.isBlank() || EMAIL_REGEX.matches(email.trim())
}
