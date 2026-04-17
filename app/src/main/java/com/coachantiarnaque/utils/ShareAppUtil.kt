package com.coachantiarnaque.utils

import android.content.Context
import android.content.Intent

/**
 * Utilitaire pour partager l'application avec un proche.
 */
object ShareAppUtil {

    private const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.coachantiarnaque"

    private val SHARE_MESSAGE = """
        Bonjour,
        
        J'utilise cette application pour vérifier si des messages ou des sites sont sécuritaires. Elle est très simple à utiliser et peut aider à éviter les arnaques.
        
        Tu peux l'essayer ici :
        $PLAY_STORE_URL
        
        C'est gratuit et ça prend 2 minutes à installer.
    """.trimIndent()

    /**
     * Ouvre le menu de partage Android avec le message de recommandation.
     */
    fun shareApp(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Coach Anti-Arnaque — Application recommandée")
                putExtra(Intent.EXTRA_TEXT, SHARE_MESSAGE)
            }
            context.startActivity(
                Intent.createChooser(intent, "Partager l'application via...")
            )
        } catch (_: Exception) {
            // Aucune app de partage disponible — silencieux
        }
    }
}
