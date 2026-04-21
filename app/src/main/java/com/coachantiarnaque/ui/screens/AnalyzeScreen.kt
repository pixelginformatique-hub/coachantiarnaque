package com.coachantiarnaque.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coachantiarnaque.R
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.components.LargeOutlinedButton
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(onBack: () -> Unit, onPickSms: () -> Unit, viewModel: HomeViewModel) {
    var messageText by remember { mutableStateOf("") }
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val error by viewModel.analysisError.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBg,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.analyze_sms_title), color = TextWhite) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = NeonPurple) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))
            LargeButton(text = stringResource(R.string.btn_pick_sms), onClick = onPickSms)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = NeonPurple.copy(alpha = 0.2f))
                Text("  ${stringResource(R.string.or_paste_text)}  ", style = MaterialTheme.typography.bodySmall, color = TextGray)
                HorizontalDivider(Modifier.weight(1f), color = NeonPurple.copy(alpha = 0.2f))
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(messageText, { messageText = it }, Modifier.fillMaxWidth().height(180.dp), placeholder = { Text(stringResource(R.string.paste_placeholder), style = MaterialTheme.typography.bodyMedium, color = TextGray.copy(alpha = 0.5f)) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple, unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f), cursorColor = NeonPurple, focusedContainerColor = DarkCard, unfocusedContainerColor = DarkCard))
            Spacer(Modifier.height(20.dp))
            if (isAnalyzing) { CircularProgressIndicator(Modifier.size(48.dp), color = NeonPurple, trackColor = DarkCard); Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.analyzing), style = MaterialTheme.typography.bodyLarge, color = NeonPurple) }
            else { LargeOutlinedButton(text = stringResource(R.string.btn_analyze_pasted), onClick = { viewModel.analyzeManualMessage(messageText); messageText = ""; onBack() }, enabled = messageText.isNotBlank()) }
            if (error != null) { Spacer(Modifier.height(16.dp)); Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ScamRed.copy(alpha = 0.15f))) { Text(error!!, style = MaterialTheme.typography.bodyLarge, color = ScamRed, modifier = Modifier.padding(16.dp)) } }
        }
    }
}
