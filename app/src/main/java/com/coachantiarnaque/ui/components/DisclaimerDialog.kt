package com.coachantiarnaque.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coachantiarnaque.ui.theme.*

private const val PREFS_NAME = "coach_anti_arnaque_prefs"
private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"

/**
 * Vérifie si l'avertissement a déjà été accepté.
 */
fun hasAcceptedDisclaimer(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
}

/**
 * Sauvegarde l'acceptation de l'avertissement.
 */
fun saveDisclaimerAccepted(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, true).apply()
}

/**
 * Dialogue d'avertissement affiché à la première utilisation.
 */
@Composable
fun DisclaimerDialog(
    onAccept: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Ne pas fermer sans accepter */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    SuspiciousOrange.copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⚖️", fontSize = 40.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Avertissement important",
                    style = MaterialTheme.typography.titleLarge,
                    color = SuspiciousOrange,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cette application fournit des indications à titre informatif uniquement, " +
                        "basées sur les informations publiques disponibles au moment de l'analyse.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Les résultats ne constituent en aucun cas une garantie de sécurité. " +
                        "Un message, un e-mail ou un site web jugé « sécuritaire » par l'application " +
                        "pourrait néanmoins s'avérer frauduleux.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Les créateurs de cette application ne peuvent être tenus responsables " +
                        "en cas de fraude non détectée ou de toute perte résultant de l'utilisation " +
                        "de cette application.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "En cas de doute, contactez toujours directement l'organisme concerné " +
                        "ou demandez l'avis d'un proche.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                LargeButton(
                    text = "✅  J'ai compris et j'accepte",
                    onClick = onAccept
                )
            }
        }
    }
}
