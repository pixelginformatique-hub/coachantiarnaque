package com.coachantiarnaque.ui.screens

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import com.coachantiarnaque.domain.engine.EmailRiskLevel
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.components.ResultCard
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.ShareMessageBuilder
import com.coachantiarnaque.viewmodel.EmailAnalysisViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailResultScreen(
    onBack: () -> Unit,
    viewModel: EmailAnalysisViewModel
) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // TTS
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val t = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale.FRENCH
        }
        tts = t
        onDispose { t.shutdown() }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Résultat de l'analyse", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = NeonPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        val res = result
        if (res == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonPurple)
            }
            return@Scaffold
        }

        // Mapper vers ResultType pour réutiliser le ResultCard avec animation
        val resultType = when (res.riskLevel) {
            EmailRiskLevel.LOW -> ResultType.SAFE
            EmailRiskLevel.MODERATE -> ResultType.SUSPICIOUS
            EmailRiskLevel.HIGH -> ResultType.SCAM
        }

        val reasonColor = when (res.riskLevel) {
            EmailRiskLevel.LOW -> SafeGreen
            EmailRiskLevel.MODERATE -> SuspiciousOrange
            EmailRiskLevel.HIGH -> ScamRed
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Carte résultat avec animation de bordure
            ResultCard(resultType = resultType)

            Spacer(modifier = Modifier.height(24.dp))

            // Raisons
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NeonPurple)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Détails de l'analyse",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            res.reasons.forEach { reason ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, reasonColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = reasonColor.copy(alpha = 0.08f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Text(
                            text = when (res.riskLevel) {
                                EmailRiskLevel.LOW -> "✅"
                                EmailRiskLevel.MODERATE -> "⚠️"
                                EmailRiskLevel.HIGH -> "🚨"
                            },
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(text = reason, style = MaterialTheme.typography.bodyMedium, color = TextWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Boutons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val levelText = when (res.riskLevel) {
                            EmailRiskLevel.LOW -> "Cet email présente peu de signes de risque."
                            EmailRiskLevel.MODERATE -> "Attention, cet email présente certains signes de risque."
                            EmailRiskLevel.HIGH -> "Attention, cet email présente plusieurs signes de risque. Soyez prudent."
                        }
                        tts?.speak("$levelText ${res.reasons.joinToString(". ")}", TextToSpeech.QUEUE_FLUSH, null, null)
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f)))
                    )
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Écouter", color = NeonPurple, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedButton(
                    onClick = {
                        val levelLabel = when (res.riskLevel) {
                            EmailRiskLevel.LOW -> "Faible risque"
                            EmailRiskLevel.MODERATE -> "Risque modéré"
                            EmailRiskLevel.HIGH -> "Risque élevé"
                        }
                        val shareText = ShareMessageBuilder.forEmail(
                            content = viewModel.analyzedContent.value,
                            senderEmail = viewModel.analyzedSender.value,
                            resultLabel = levelLabel,
                            reasons = res.reasons
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Envoyer à..."))
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f)))
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Envoyer", color = NeonPurple, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
