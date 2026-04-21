package com.coachantiarnaque.ui.screens

import android.speech.tts.TextToSpeech
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coachantiarnaque.R
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.components.*
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

    var showDisclaimer by remember { mutableStateOf(!hasAcceptedDisclaimer(context)) }
    if (showDisclaimer) {
        DisclaimerDialog(onAccept = { saveDisclaimerAccepted(context); showDisclaimer = false })
    }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val t = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale.getDefault() }
        tts = t; onDispose { t.shutdown() }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    TextButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_history), color = NeonPurple, style = MaterialTheme.typography.bodySmall)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text("🛡️", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, color = TextWhite, textAlign = TextAlign.Center)
            Text(stringResource(R.string.app_subtitle), style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)

            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(2.dp)).background(NeonPurple))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.last_message_analyzed), style = MaterialTheme.typography.titleMedium, color = TextWhite)
            }
            Spacer(Modifier.height(16.dp))

            if (lastMessage != null) {
                val msg = lastMessage!!
                ResultCard(resultType = msg.resultType)
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth().border(1.dp, NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Column(Modifier.padding(16.dp)) { msg.reasons.take(2).forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, color = TextGray, modifier = Modifier.padding(vertical = 4.dp)) } }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            val txt = when (msg.resultType) { ResultType.SAFE -> context.getString(R.string.tts_safe); ResultType.SUSPICIOUS -> context.getString(R.string.tts_suspicious); ResultType.SCAM -> context.getString(R.string.tts_scam) }
                            tts?.speak("$txt ${msg.reasons.joinToString(". ")}", TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))))
                    ) { Icon(Icons.Default.VolumeUp, null, tint = NeonPurple, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.btn_listen), color = NeonPurple, style = MaterialTheme.typography.bodySmall) }
                    OutlinedButton(
                        onClick = { onNavigateToDetail(msg.id) },
                        Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))))
                    ) { Text(stringResource(R.string.btn_detail), color = NeonPurple, style = MaterialTheme.typography.bodySmall) }
                }
            } else {
                Card(Modifier.fillMaxWidth().border(1.dp, NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 40.sp); Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.no_message_title), style = MaterialTheme.typography.titleMedium, color = TextWhite, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.no_message_subtitle), style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            LargeButton(text = stringResource(R.string.btn_analyze_sms), onClick = onNavigateToAnalyze)
            Spacer(Modifier.height(14.dp))
            LargeButton(text = stringResource(R.string.btn_check_website), onClick = onNavigateToWebsiteCheck)
            Spacer(Modifier.height(14.dp))
            LargeButton(text = stringResource(R.string.btn_analyze_email), onClick = onNavigateToEmailAnalysis)
            Spacer(Modifier.height(14.dp))
            LargeOutlinedButton(text = stringResource(R.string.btn_ask_contact), onClick = onNavigateToHelp)
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = NeonPurple.copy(alpha = 0.15f))
            Spacer(Modifier.height(16.dp))
            LargeOutlinedButton(text = stringResource(R.string.btn_share_app), onClick = { ShareAppUtil.shareApp(context) })
            Spacer(Modifier.height(32.dp))
        }
    }
}
