package com.coachantiarnaque.domain.engine

import com.coachantiarnaque.R
import com.coachantiarnaque.utils.StringProvider
import java.net.URI

enum class RiskLevel { LOW, MODERATE, HIGH }

data class WebsiteAnalysisResult(
    val score: Int, val riskLevel: RiskLevel, val reasons: List<String>, val domain: String
) {
    companion object {
        fun fromScore(score: Int, reasons: List<String>, domain: String): WebsiteAnalysisResult {
            val level = when { score >= 6 -> RiskLevel.HIGH; score >= 3 -> RiskLevel.MODERATE; else -> RiskLevel.LOW }
            return WebsiteAnalysisResult(score, level, reasons, domain)
        }
    }
}

class WebsiteAnalysisEngine {
    companion object {
        val SUSPICIOUS_TLDS = listOf(".xyz",".top",".club",".work",".buzz",".icu",".site",".online",".fun",".space",".click",".link",".info",".tk",".ml",".ga",".cf",".gq",".pw",".cc",".ws",".bid",".stream",".racing")
        val BRAND_KEYWORDS = listOf("nike","adidas","amazon","apple","google","facebook","instagram","paypal","netflix","microsoft","samsung","louis-vuitton","gucci","chanel","hermes","dior","walmart","costco","bestbuy","ebay","aliexpress","banque","desjardins","rbc","td","bmo","scotiabank")
        val SUSPICIOUS_DOMAIN_WORDS = listOf("cheap","deal","free","promo","discount","sale","login","secure","verify","update","account","officiel","official","support","service","help","gratuit","cadeau","gagnant","winner","prize","gift","reward","claim","limited","offer")
    }

    fun extractDomain(input: String): String? {
        val url = if (!input.startsWith("http")) "https://$input" else input
        return try { val uri = URI(url); uri.host?.removePrefix("www.") } catch (_: Exception) { null }
    }

    fun isValidUrl(input: String): Boolean {
        val url = if (!input.startsWith("http")) "https://$input" else input
        return try { val uri = URI(url); uri.host != null && uri.host!!.contains(".") } catch (_: Exception) { false }
    }

    fun analyze(url: String, apiResults: WebsiteApiResults = WebsiteApiResults(), sp: StringProvider? = null): WebsiteAnalysisResult {
        var score = 0; val reasons = mutableListOf<String>(); val domain = extractDomain(url) ?: url

        val tld = SUSPICIOUS_TLDS.firstOrNull { domain.endsWith(it) }
        if (tld != null) { score += 1; reasons.add(sp?.getString(R.string.reason_suspicious_tld, tld) ?: "L'extension du site ($tld) est inhabituelle") }

        val brandFound = BRAND_KEYWORDS.firstOrNull { domain.lowercase().contains(it) }
        if (brandFound != null) {
            val isLikelyFake = !domain.equals("$brandFound.com", true) && !domain.equals("$brandFound.ca", true) && !domain.equals("$brandFound.fr", true) && !domain.endsWith(".$brandFound.com", true)
            if (isLikelyFake) { score += 2; reasons.add(sp?.getString(R.string.reason_brand_in_domain, brandFound) ?: "Le nom du site utilise une marque connue ($brandFound) de façon inhabituelle") }
        }

        if (SUSPICIOUS_DOMAIN_WORDS.any { domain.lowercase().contains(it) }) { score += 1; reasons.add(sp?.getString(R.string.reason_suspicious_domain_words) ?: "Le nom du site contient des mots souvent associés à des sites à risque") }
        if (domain.length > 30) { score += 1; reasons.add(sp?.getString(R.string.reason_long_domain) ?: "Le nom du site est inhabituellement long") }
        if (domain.count { it == '-' } >= 2) { score += 1; reasons.add(sp?.getString(R.string.reason_many_dashes) ?: "Le nom du site contient plusieurs tirets, ce qui est inhabituel") }
        if (domain.split(".").size > 3) { score += 1; reasons.add(sp?.getString(R.string.reason_many_subdomains) ?: "Le site utilise plusieurs sous-domaines, ce qui peut être suspect") }

        if (apiResults.isMalicious) { score += 3; reasons.add(sp?.getString(R.string.reason_safe_browsing_hit) ?: "Ce lien est signalé comme potentiellement dangereux par les bases de sécurité") }
        if (apiResults.virusTotalMalicious > 3) { score += 3; reasons.add(sp?.getString(R.string.reason_vt_high) ?: "Plusieurs services de sécurité signalent ce site comme à risque") }
        else if (apiResults.virusTotalMalicious > 0) { score += 1; reasons.add(sp?.getString(R.string.reason_vt_low) ?: "Certains services de sécurité ont émis des alertes sur ce site") }

        when {
            apiResults.domainAgeDays in 0..6 -> { score += 3; reasons.add(sp?.getString(R.string.reason_domain_very_new) ?: "Le site est très récent (moins d'une semaine)") }
            apiResults.domainAgeDays in 7..29 -> { score += 2; reasons.add(sp?.getString(R.string.reason_domain_new) ?: "Le site est récent (moins d'un mois)") }
            apiResults.domainAgeDays in 30..89 -> { score += 1; reasons.add(sp?.getString(R.string.reason_domain_recent) ?: "Le site a été créé il y a moins de 3 mois") }
        }

        if (reasons.isEmpty()) { reasons.add(sp?.getString(R.string.reason_website_none) ?: "Aucun signe de risque particulier détecté pour ce site") }
        return WebsiteAnalysisResult.fromScore(score, reasons, domain)
    }
}

data class WebsiteApiResults(val isMalicious: Boolean = false, val virusTotalMalicious: Int = 0, val domainAgeDays: Int = -1)
