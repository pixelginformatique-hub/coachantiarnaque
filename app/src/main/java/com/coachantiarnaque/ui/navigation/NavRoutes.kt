package com.coachantiarnaque.ui.navigation

/**
 * Routes de navigation de l'application.
 */
object NavRoutes {
    const val HOME = "home"
    const val ANALYZE = "analyze"
    const val DETAIL = "detail/{messageId}"
    const val HISTORY = "history"
    const val HELP = "help"
    const val SMS_PICKER = "sms_picker"
    const val WEBSITE_CHECK = "website_check"
    const val EMAIL_ANALYSIS = "email_analysis"
    const val EMAIL_RESULT = "email_result"

    fun detailRoute(messageId: Long) = "detail/$messageId"
}
