package com.coachantiarnaque.ui.components

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coachantiarnaque.R
import com.coachantiarnaque.ui.theme.*

private const val PREFS_NAME = "coach_anti_arnaque_prefs"
private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"

fun hasAcceptedDisclaimer(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
}

fun saveDisclaimerAccepted(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, true).apply()
}

@Composable
fun DisclaimerDialog(onAccept: () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, SuspiciousOrange.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⚖️", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.disclaimer_title), style = MaterialTheme.typography.titleLarge, color = SuspiciousOrange, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.disclaimer_p1), style = MaterialTheme.typography.bodyMedium, color = TextWhite, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.disclaimer_p2), style = MaterialTheme.typography.bodyMedium, color = TextWhite, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.disclaimer_p3), style = MaterialTheme.typography.bodyMedium, color = TextWhite, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.disclaimer_p4), style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                LargeButton(text = stringResource(R.string.btn_accept), onClick = onAccept)
            }
        }
    }
}
