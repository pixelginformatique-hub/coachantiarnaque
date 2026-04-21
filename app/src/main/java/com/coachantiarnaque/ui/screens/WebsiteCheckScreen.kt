package com.coachantiarnaque.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coachantiarnaque.R
import com.coachantiarnaque.domain.engine.RiskLevel
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.ShareMessageBuilder
import com.coachantiarnaque.viewmodel.WebsiteCheckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteCheckScreen(onBack: () -> Unit, onAskContact: () -> Unit, viewModel: WebsiteCheckViewModel = viewModel()) {
    var urlText by remember { mutableStateOf("") }
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val analyzedUrl by viewModel.analyzedUrl.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = DarkBg,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.website_check_title), color = TextWhite) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NeonPurple) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌐", fontSize = 40.sp); Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.website_check_heading), style = MaterialTheme.typography.titleLarge, color = TextWhite, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.website_check_subtitle), style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(urlText, { urlText = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.website_url_placeholder), style = MaterialTheme.typography.bodyLarge, color = TextGray.copy(alpha = 0.5f)) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple, unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f), cursorColor = NeonPurple, focusedContainerColor = DarkCard, unfocusedContainerColor = DarkCard))
            Spacer(Modifier.height(20.dp))
            if (isAnalyzing) { CircularProgressIndicator(Modifier.size(48.dp), color = NeonPurple, trackColor = DarkCard); Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.checking), style = MaterialTheme.typography.bodyLarge, color = NeonPurple) }
            else { LargeButton(text = stringResource(R.string.btn_analyze_site), onClick = { viewModel.analyzeWebsite(urlText) }, enabled = urlText.isNotBlank()) }
            if (error != null) { Spacer(Modifier.height(16.dp)); Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ScamRed.copy(alpha = 0.15f))) { Text(error!!, style = MaterialTheme.typography.bodyLarge, color = ScamRed, modifier = Modifier.padding(16.dp)) } }

            AnimatedVisibility(result != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                result?.let { res ->
                    val (color, emoji, label, message) = when (res.riskLevel) { RiskLevel.LOW -> R4(SafeGreen, "🟢", stringResource(R.string.risk_low), stringResource(R.string.risk_low_msg)); RiskLevel.MODERATE -> R4(SuspiciousOrange, "🟠", stringResource(R.string.risk_moderate), stringResource(R.string.risk_moderate_msg)); RiskLevel.HIGH -> R4(ScamRed, "🔴", stringResource(R.string.risk_high), stringResource(R.string.risk_high_msg)) }
                    Column {
                        Spacer(Modifier.height(28.dp))
                        Card(Modifier.fillMaxWidth().border(1.dp, Brush.verticalGradient(listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.1f))), RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                            Box(Modifier.fillMaxWidth().background(Brush.radialGradient(listOf(color.copy(alpha = 0.12f), Color.Transparent), radius = 400f))) {
                                Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.size(72.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)).border(2.dp, color.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 36.sp) }
                                    Spacer(Modifier.height(14.dp)); Text(label, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(8.dp)); Text(message, style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(8.dp)); Text(res.domain, style = MaterialTheme.typography.bodySmall, color = NeonPurple.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(2.dp)).background(NeonPurple)); Spacer(Modifier.width(10.dp)); Text(stringResource(R.string.analysis_details), style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(8.dp))
                        res.reasons.forEach { reason -> Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) { Text(when (res.riskLevel) { RiskLevel.LOW -> "✅"; RiskLevel.MODERATE -> "⚠️"; RiskLevel.HIGH -> "🚨" }, modifier = Modifier.padding(end = 10.dp)); Text(reason, style = MaterialTheme.typography.bodyMedium, color = TextWhite) } } }
                        Spacer(Modifier.height(24.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(if (!analyzedUrl.startsWith("http")) "https://$analyzedUrl" else analyzedUrl))) }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))))) { Icon(Icons.Default.OpenInBrowser, null, tint = NeonPurple, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.btn_open_site), color = NeonPurple, style = MaterialTheme.typography.bodySmall) }
                            OutlinedButton(onClick = { val lbl = when (res.riskLevel) { RiskLevel.LOW -> context.getString(R.string.risk_low); RiskLevel.MODERATE -> context.getString(R.string.risk_moderate); RiskLevel.HIGH -> context.getString(R.string.risk_high) }; context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, ShareMessageBuilder.forWebsite(context, analyzedUrl, lbl, res.reasons)) }, context.getString(R.string.share_via))) }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))))) { Icon(Icons.Default.Share, null, tint = NeonPurple, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.btn_ask_opinion), color = NeonPurple, style = MaterialTheme.typography.bodySmall) }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

private data class R4(val color: Color, val emoji: String, val label: String, val message: String)
