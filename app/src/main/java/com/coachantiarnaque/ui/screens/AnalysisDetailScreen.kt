package com.coachantiarnaque.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coachantiarnaque.R
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.components.LargeOutlinedButton
import com.coachantiarnaque.ui.components.ResultCard
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.ShareMessageBuilder
import com.coachantiarnaque.viewmodel.AnalysisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisDetailScreen(messageId: Long, onBack: () -> Unit, viewModel: AnalysisViewModel = viewModel()) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(messageId) { viewModel.loadMessage(messageId) }

    Scaffold(
        containerColor = DarkBg,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.analysis_result_title), color = TextWhite) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NeonPurple) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)) }
    ) { padding ->
        val msg = message
        if (msg == null) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NeonPurple) }; return@Scaffold }

        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            ResultCard(resultType = msg.resultType)
            Spacer(Modifier.height(20.dp))
            SectionHeader(stringResource(R.string.message_analyzed))
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth().border(1.dp, NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) { Text(msg.content, style = MaterialTheme.typography.bodyLarge, color = TextWhite, modifier = Modifier.padding(16.dp)) }
            Spacer(Modifier.height(8.dp))
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(stringResource(R.string.analyzed_on, dateFormat.format(Date(msg.timestamp))), style = MaterialTheme.typography.bodySmall, color = TextGray)
            Spacer(Modifier.height(20.dp))
            SectionHeader(stringResource(R.string.why_this_result))
            Spacer(Modifier.height(8.dp))
            val reasonColor = when (msg.resultType) { ResultType.SAFE -> SafeGreen; ResultType.SUSPICIOUS -> SuspiciousOrange; ResultType.SCAM -> ScamRed }
            msg.reasons.forEach { reason ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, reasonColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = reasonColor.copy(alpha = 0.08f))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) { Text(when (msg.resultType) { ResultType.SAFE -> "✅"; ResultType.SUSPICIOUS -> "⚠️"; ResultType.SCAM -> "🚨" }, modifier = Modifier.padding(end = 10.dp)); Text(reason, style = MaterialTheme.typography.bodyMedium, color = TextWhite) }
                }
            }
            Spacer(Modifier.height(32.dp))
            LargeButton(text = stringResource(R.string.btn_send_contact), onClick = {
                val typeLabel = when (msg.resultType) { ResultType.SAFE -> context.getString(R.string.result_safe); ResultType.SUSPICIOUS -> context.getString(R.string.result_suspicious); ResultType.SCAM -> context.getString(R.string.result_scam) }
                val shareText = ShareMessageBuilder.forSms(context, msg.content, msg.senderNumber, typeLabel, msg.reasons)
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }, context.getString(R.string.share_via)))
            })
            Spacer(Modifier.height(12.dp))
            LargeOutlinedButton(text = stringResource(R.string.btn_delete), onClick = { viewModel.deleteMessage(msg.id) { onBack() } })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(2.dp)).background(NeonPurple)); Spacer(Modifier.width(10.dp)); Text(title, style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold) }
}
