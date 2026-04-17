package com.coachantiarnaque.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.components.LargeOutlinedButton
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    onBack: () -> Unit,
    onPickSms: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val error by viewModel.analysisError.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Analyser un message", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = NeonPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Bouton principal : choisir un SMS
            LargeButton(
                text = "📨  Choisir un SMS reçu",
                onClick = onPickSms
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Séparateur
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = NeonPurple.copy(alpha = 0.2f)
                )
                Text(
                    text = "  ou collez le texte  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = NeonPurple.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = {
                    Text(
                        "Collez le message ici...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray.copy(alpha = 0.5f)
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f),
                    cursorColor = NeonPurple,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = NeonPurple,
                    trackColor = DarkCard
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Analyse en cours...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonPurple
                )
            } else {
                LargeOutlinedButton(
                    text = "🔍  Analyser le texte collé",
                    onClick = {
                        viewModel.analyzeManualMessage(messageText)
                        messageText = ""
                        onBack()
                    },
                    enabled = messageText.isNotBlank()
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ScamRed.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ScamRed,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
