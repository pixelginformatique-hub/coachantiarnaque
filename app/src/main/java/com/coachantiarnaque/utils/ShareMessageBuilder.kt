package com.coachantiarnaque.utils

/**
 * Utilitaire centralisé pour construire les messages de partage à un proche.
 * Format standardisé pour toutes les analyses (SMS, email, site web).
 */
object ShareMessageBuilder {

    private const val INTRO = "Bonjour,\n\nJ'utilise l'application Coach Anti-Arnaque pour vérifier les messages suspects. J'aimerais avoir ton avis sur celui-ci.\n"
    private const val OUTRO = "\nQu'en penses-tu ?\n\nMerci 🙏"

    /**
     * Message de partage pour une analyse SMS.
     */
    fun forSms(
        content: String,
        senderNumber: String?,
        resultLabel: String,
        reasons: List<String>
    ): String = buildString {
        appendLine(INTRO)
        appendLine("📱 Analyse d'un SMS")
        appendLine("━━━━━━━━━━━━━━━━━━")
        if (!senderNumber.isNullOrBlank()) {
            appendLine("Expéditeur : $senderNumber")
        }
        appendLine("Résultat : $resultLabel")
        appendLine()
        appendLine("Message reçu :")
        appendLine("« $content »")
        appendLine()
        appendLine("Raisons détectées :")
        reasons.forEach { appendLine("• $it") }
        append(OUTRO)
    }

    /**
     * Message de partage pour une analyse email.
     */
    fun forEmail(
        content: String,
        senderEmail: String?,
        resultLabel: String,
        reasons: List<String>
    ): String = buildString {
        appendLine(INTRO)
        appendLine("📧 Analyse d'un e-mail")
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        if (!senderEmail.isNullOrBlank()) {
            appendLine("Expéditeur : $senderEmail")
        }
        appendLine("Résultat : $resultLabel")
        appendLine()
        appendLine("Contenu de l'e-mail :")
        appendLine("« ${content.take(500)} »")
        appendLine()
        appendLine("Raisons détectées :")
        reasons.forEach { appendLine("• $it") }
        append(OUTRO)
    }

    /**
     * Message de partage pour une vérification de site web.
     */
    fun forWebsite(
        url: String,
        resultLabel: String,
        reasons: List<String>
    ): String = buildString {
        appendLine(INTRO)
        appendLine("🌐 Vérification d'un site web")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("Site vérifié : $url")
        appendLine("Résultat : $resultLabel")
        appendLine()
        appendLine("Détails :")
        reasons.forEach { appendLine("• $it") }
        append(OUTRO)
    }

    /**
     * Message générique pour demander à un proche (écran Aide).
     */
    fun forHelpScreen(
        content: String?,
        senderNumber: String?,
        resultLabel: String?,
        reasons: List<String>?
    ): String = buildString {
        appendLine(INTRO)
        if (content != null) {
            appendLine("📱 Message suspect reçu")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            if (!senderNumber.isNullOrBlank()) {
                appendLine("Expéditeur : $senderNumber")
            }
            if (resultLabel != null) {
                appendLine("Résultat de l'analyse : $resultLabel")
            }
            appendLine()
            appendLine("Message :")
            appendLine("« $content »")
            if (!reasons.isNullOrEmpty()) {
                appendLine()
                appendLine("Raisons détectées :")
                reasons.forEach { appendLine("• $it") }
            }
        } else {
            appendLine("J'ai reçu un message suspect et j'aimerais ton avis.")
        }
        append(OUTRO)
    }
}
