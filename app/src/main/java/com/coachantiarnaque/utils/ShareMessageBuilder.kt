package com.coachantiarnaque.utils

import android.content.Context
import com.coachantiarnaque.R

/**
 * Utilitaire centralisé pour construire les messages de partage.
 * Utilise les ressources string pour le support multilingue.
 */
object ShareMessageBuilder {

    fun forSms(ctx: Context, content: String, senderNumber: String?, resultLabel: String, reasons: List<String>): String = buildString {
        appendLine(ctx.getString(R.string.share_intro))
        appendLine(ctx.getString(R.string.share_sms_header))
        appendLine("━━━━━━━━━━━━━━━━━━")
        if (!senderNumber.isNullOrBlank()) appendLine(ctx.getString(R.string.share_sender, senderNumber))
        appendLine(ctx.getString(R.string.share_result, resultLabel))
        appendLine()
        appendLine(ctx.getString(R.string.share_message_received))
        appendLine("« $content »")
        appendLine()
        appendLine(ctx.getString(R.string.share_reasons))
        reasons.forEach { appendLine("• $it") }
        append(ctx.getString(R.string.share_outro))
    }

    fun forEmail(ctx: Context, content: String, senderEmail: String?, resultLabel: String, reasons: List<String>): String = buildString {
        appendLine(ctx.getString(R.string.share_intro))
        appendLine(ctx.getString(R.string.share_email_header))
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        if (!senderEmail.isNullOrBlank()) appendLine(ctx.getString(R.string.share_sender, senderEmail))
        appendLine(ctx.getString(R.string.share_result, resultLabel))
        appendLine()
        appendLine(ctx.getString(R.string.share_email_content))
        appendLine("« ${content.take(500)} »")
        appendLine()
        appendLine(ctx.getString(R.string.share_reasons))
        reasons.forEach { appendLine("• $it") }
        append(ctx.getString(R.string.share_outro))
    }

    fun forWebsite(ctx: Context, url: String, resultLabel: String, reasons: List<String>): String = buildString {
        appendLine(ctx.getString(R.string.share_intro))
        appendLine(ctx.getString(R.string.share_website_header))
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine(ctx.getString(R.string.share_site_checked, url))
        appendLine(ctx.getString(R.string.share_result, resultLabel))
        appendLine()
        appendLine(ctx.getString(R.string.share_details))
        reasons.forEach { appendLine("• $it") }
        append(ctx.getString(R.string.share_outro))
    }

    fun forHelpScreen(ctx: Context, content: String?, senderNumber: String?, resultLabel: String?, reasons: List<String>?): String = buildString {
        appendLine(ctx.getString(R.string.share_intro))
        if (content != null) {
            appendLine(ctx.getString(R.string.share_help_suspect))
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            if (!senderNumber.isNullOrBlank()) appendLine(ctx.getString(R.string.share_sender, senderNumber))
            if (resultLabel != null) appendLine(ctx.getString(R.string.share_help_result, resultLabel))
            appendLine()
            appendLine(ctx.getString(R.string.share_message_received))
            appendLine("« $content »")
            if (!reasons.isNullOrEmpty()) { appendLine(); appendLine(ctx.getString(R.string.share_reasons)); reasons.forEach { appendLine("• $it") } }
        } else {
            appendLine(ctx.getString(R.string.share_help_generic))
        }
        append(ctx.getString(R.string.share_outro))
    }
}
