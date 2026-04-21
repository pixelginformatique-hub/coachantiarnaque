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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coachantiarnaque.R
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.viewmodel.EmailAnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailAnalysisScreen(onBack: () -> Unit, onResultReady: () -> Unit, viewModel: EmailAnalysisViewModel) {
    val sharedContent by viewModel.sharedContent.collectAsStateWithLifecycle()
    var emailContent by remember { mutableStateOf("") }
    var senderEmail by remember { mutableStateOf("") }
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(sharedContent) { if (sharedContent.isNotBlank() && emailContent.isBlank()) emailContent = sharedContent }
    LaunchedEffect(result) { if (result != null) onResultReady() }

    Scaffold(
        containerColor = DarkBg,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.email_analysis_title), color = TextWhite) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NeonPurple) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📧", fontSize = 40.sp); Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.email_analysis_title), style = MaterialTheme.typography.titleLarge, color = TextWhite, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.email_analysis_subtitle), style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(senderEmail, { senderEmail = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.email_sender_label), color = TextGray) }, placeholder = { Text(stringResource(R.string.email_sender_placeholder), color = TextGray.copy(alpha = 0.4f)) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite), shape = RoundedCornerShape(14.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple, unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f), cursorColor = NeonPurple, focusedContainerColor = DarkCard, unfocusedContainerColor = DarkCard, focusedLabelColor = NeonPurple))
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(emailContent, { emailContent = it }, Modifier.fillMaxWidth().height(200.dp), placeholder = { Text(stringResource(R.string.email_content_placeholder), style = MaterialTheme.typography.bodyMedium, color = TextGray.copy(alpha = 0.4f)) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite), shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple, unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f), cursorColor = NeonPurple, focusedContainerColor = DarkCard, unfocusedContainerColor = DarkCard))
            Spacer(Modifier.height(20.dp))
            if (isAnalyzing) { CircularProgressIndicator(Modifier.size(48.dp), color = NeonPurple, trackColor = DarkCard); Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.analyzing), style = MaterialTheme.typography.bodyLarge, color = NeonPurple) }
            else { LargeButton(text = stringResource(R.string.btn_analyze_email_action), onClick = { viewModel.analyzeEmail(emailContent, senderEmail.ifBlank { null }) }, enabled = emailContent.isNotBlank()) }
            if (error != null) { Spacer(Modifier.height(16.dp)); Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ScamRed.copy(alpha = 0.15f))) { Text(error!!, style = MaterialTheme.typography.bodyLarge, color = ScamRed, modifier = Modifier.padding(16.dp)) } }
        }
    }
}
