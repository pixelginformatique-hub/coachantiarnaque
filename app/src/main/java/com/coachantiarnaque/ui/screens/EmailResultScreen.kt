package com.coachantiarnaque.ui.screens

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coachantiarnaque.R
import com.coachantiarnaque.domain.engine.EmailRiskLevel
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.components.ResultCard
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.ShareMessageBuilder
import com.coachantiarnaque.viewmodel.EmailAnalysisViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailResultScreen(onBack: () -> Unit, viewModel: EmailAnalysisViewModel) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) { val t = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale.getDefault() }; tts = t; onDispose { t.shutdown() } }

    Scaffold(
        containerColor = DarkBg,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.email_result_title), color = TextWhite) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NeonPurple) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)) }
    ) { padding ->
        val res = result
        if (res == null) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NeonPurple) }; return@Scaffold }

        val resultType = when (res.riskLevel) { EmailRiskLevel.LOW -> ResultType.SAFE; EmailRiskLevel.MODERATE -> ResultType.SUSPICIOUS; EmailRiskLevel.HIGH -> ResultType.SCAM }
        val reasonColor = when (res.riskLevel) { EmailRiskLevel.LOW -> SafeGreen; EmailRiskLevel.MODERATE -> SuspiciousOrange; EmailRiskLevel.HIGH -> ScamRed }

        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ResultCard(resultType = resultType)
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(2.dp)).background(NeonPurple)); Spacer(Modifier.width(10.dp)); Text(stringResource(R.string.analysis_details), style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            res.reasons.forEach { reason -> Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, reasonColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = reasonColor.copy(alpha = 0.08f))) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) { Text(when (res.riskLevel) { EmailRiskLevel.LOW -> "✅"; EmailRiskLevel.MODERATE -> "⚠️"; EmailRiskLevel.HIGH -> "🚨" }, modifier = Modifier.padding(end = 10.dp)); Text(reason, style = MaterialTheme.typography.bodyMedium, color = TextWhite) } } }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { val txt = when (res.riskLevel) { EmailRiskLevel.LOW -> context.getString(R.string.tts_email_low); EmailRiskLevel.MODERATE -> context.getString(R.string.tts_email_moderate); EmailRiskLevel.HIGH -> context.getString(R.string.tts_email_high) }; tts?.speak("$txt ${res.reasons.joinToString(". ")}", TextToSpeech.QUEUE_FLUSH, null, null) }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))))) { Icon(Icons.Default.VolumeUp, null, tint = NeonPurple, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.btn_listen), color = NeonPurple, style = MaterialTheme.typography.bodySmall) }
                OutlinedButton(onClick = { val lbl = when (res.riskLevel) { EmailRiskLevel.LOW -> context.getString(R.string.risk_low); EmailRiskLevel.MODERATE -> context.getString(R.string.risk_moderate); EmailRiskLevel.HIGH -> context.getString(R.string.risk_high) }; context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, ShareMessageBuilder.forEmail(context, viewModel.analyzedContent.value, viewModel.analyzedSender.value, lbl, res.reasons)) }, context.getString(R.string.share_via))) }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))))) { Icon(Icons.Default.Share, null, tint = NeonPurple, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.btn_send), color = NeonPurple, style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
