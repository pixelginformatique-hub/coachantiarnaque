package com.coachantiarnaque.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coachantiarnaque.R
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.components.LargeOutlinedButton
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.ShareAppUtil
import com.coachantiarnaque.utils.ShareMessageBuilder
import com.coachantiarnaque.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit, viewModel: HomeViewModel) {
    val context = LocalContext.current
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    val lastMessage by viewModel.lastMessage.collectAsStateWithLifecycle()

    fun buildShareText(): String {
        val msg = lastMessage
        val typeLabel = msg?.let { when (it.resultType) { ResultType.SAFE -> context.getString(R.string.result_safe); ResultType.SUSPICIOUS -> context.getString(R.string.result_suspicious); ResultType.SCAM -> context.getString(R.string.result_scam) } }
        return ShareMessageBuilder.forHelpScreen(context, msg?.content, msg?.senderNumber, typeLabel, msg?.reasons)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.help_title), color = TextWhite) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NeonPurple) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.trusted_person), style = MaterialTheme.typography.titleLarge, color = TextWhite, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.help_subtitle), style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)

            if (lastMessage != null) {
                val msg = lastMessage!!
                val resultColor = when (msg.resultType) { ResultType.SAFE -> SafeGreen; ResultType.SUSPICIOUS -> SuspiciousOrange; ResultType.SCAM -> ScamRed }
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth().border(1.dp, resultColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = resultColor.copy(alpha = 0.08f))) {
                    Column(Modifier.padding(16.dp)) { Text(stringResource(R.string.message_preview), style = MaterialTheme.typography.bodySmall, color = resultColor); Spacer(Modifier.height(8.dp)); Text(msg.content, style = MaterialTheme.typography.bodyMedium, color = TextWhite, maxLines = 4) }
                }
            }

            Spacer(Modifier.height(24.dp))
            OutlinedTextField(contactName, { contactName = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.contact_name), color = TextGray) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple, unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f), cursorColor = NeonPurple, focusedContainerColor = DarkCard, unfocusedContainerColor = DarkCard, focusedLabelColor = NeonPurple))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(contactPhone, { contactPhone = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.contact_phone), color = TextGray) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple, unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f), cursorColor = NeonPurple, focusedContainerColor = DarkCard, unfocusedContainerColor = DarkCard, focusedLabelColor = NeonPurple))
            Spacer(Modifier.height(24.dp))

            LargeButton(text = stringResource(R.string.btn_call), onClick = { if (contactPhone.isNotBlank()) context.startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$contactPhone") }) }, enabled = contactPhone.isNotBlank(), containerColor = SafeGreen)
            Spacer(Modifier.height(12.dp))
            LargeOutlinedButton(text = stringResource(R.string.btn_sms), onClick = { if (contactPhone.isNotBlank()) context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:$contactPhone"); putExtra("sms_body", buildShareText()) }) }, enabled = contactPhone.isNotBlank())
            Spacer(Modifier.height(12.dp))
            LargeOutlinedButton(text = stringResource(R.string.btn_share_other), onClick = { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, buildShareText()) }, context.getString(R.string.share_via))) })
            Spacer(Modifier.height(32.dp))

            Card(Modifier.fillMaxWidth().border(1.dp, NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(2.dp)).background(NeonPurple)); Spacer(Modifier.width(10.dp)); Text(stringResource(R.string.security_tips_title), style = MaterialTheme.typography.titleMedium, color = NeonPurple) }
                    Spacer(Modifier.height(12.dp))
                    listOf(R.string.tip_1, R.string.tip_2, R.string.tip_3, R.string.tip_4).forEach { Text("• ${stringResource(it)}", style = MaterialTheme.typography.bodyMedium, color = TextGray, modifier = Modifier.padding(vertical = 4.dp)) }
                }
            }
            Spacer(Modifier.height(24.dp))
            LargeOutlinedButton(text = stringResource(R.string.btn_share_app), onClick = { ShareAppUtil.shareApp(context) })
            Spacer(Modifier.height(24.dp))
        }
    }
}
