package com.coachantiarnaque.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.components.LargeOutlinedButton
import com.coachantiarnaque.ui.components.ResultCard
import com.coachantiarnaque.ui.components.DisclaimerDialog
import com.coachantiarnaque.ui.components.hasAcceptedDisclaimer
import com.coachantiarnaque.ui.components.saveDisclaimerAccepted
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.ShareAppUtil
import com.coachantiarnaque.viewmodel.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAnalyze: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToWebsiteCheck: () -> Unit = {},
    onNavigateToEmailAnalysis: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val lastMessage by viewModel.lastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Avertissement première utilisation
    var showDisclaimer by remember { mutableStateOf(!hasAcceptedDisclaimer(context)) }
    if (showDisclaimer) {
        DisclaimerDialog(
            onAccept = {
                saveDisclaimerAccepted(context)
                showDisclaimer = false
            }
        )
    }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.FRENCH
            }
        }
        tts = textToSpeech
        onDispose { textToSpeech.shutdown() }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    TextButton(onClick = onNavigateToHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Historique",
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Historique", color = NeonPurple, style = MaterialTheme.typography.bodySmall)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header avec logo texte
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🛡️",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Coach Anti-Arnaque",
                style = MaterialTheme.typography.headlineLarge,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Votre protection contre les fraudes",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Section dernier message
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NeonPurple)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Dernier message analysé",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (lastMessage != null) {
                val msg = lastMessage!!

                ResultCard(resultType = msg.resultType)

                Spacer(modifier = Modifier.height(16.dp))

                // Raisons dans une carte sombre
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            NeonPurple.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        msg.reasons.take(2).forEach { reason ->
                            Text(
                                text = "• $reason",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Boutons écouter + détail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val typeText = when (msg.resultType) {
                                ResultType.SAFE -> "Ce message est sécuritaire."
                                ResultType.SUSPICIOUS -> "Attention, ce message est douteux."
                                ResultType.SCAM -> "Attention, ce message est probablement une arnaque."
                            }
                            val reasonsText = msg.reasons.joinToString(". ")
                            tts?.speak("$typeText $reasonsText", TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                            brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f)))
                        )
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Écouter", color = NeonPurple, style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedButton(
                        onClick = { onNavigateToDetail(msg.id) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                            brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f)))
                        )
                    ) {
                        Text("Voir le détail →", color = NeonPurple, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                // État vide
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            NeonPurple.copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📭", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucun message analysé",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Appuyez ci-dessous pour commencer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Boutons d'action
            LargeButton(
                text = "📱  Analyser un SMS",
                onClick = onNavigateToAnalyze
            )

            Spacer(modifier = Modifier.height(14.dp))

            LargeButton(
                text = "🌐  Vérifier un site web",
                onClick = onNavigateToWebsiteCheck
            )

            Spacer(modifier = Modifier.height(14.dp))

            LargeButton(
                text = "📧  Analyser un e-mail",
                onClick = onNavigateToEmailAnalysis
            )

            Spacer(modifier = Modifier.height(14.dp))

            LargeOutlinedButton(
                text = "👨‍👩‍👧  Demander à un proche",
                onClick = onNavigateToHelp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Séparateur
            HorizontalDivider(color = NeonPurple.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton partager l'app
            LargeOutlinedButton(
                text = "💜  Partager l'application à un proche",
                onClick = { ShareAppUtil.shareApp(context) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
