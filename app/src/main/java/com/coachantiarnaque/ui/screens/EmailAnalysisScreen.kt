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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.viewmodel.EmailAnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailAnalysisScreen(
    onBack: () -> Unit,
    onResultReady: () -> Unit,
    viewModel: EmailAnalysisViewModel
) {
    val sharedContent by viewModel.sharedContent.collectAsStateWithLifecycle()
    var emailContent by remember { mutableStateOf("") }
    var senderEmail by remember { mutableStateOf("") }
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Pré-remplir si contenu partagé
    LaunchedEffect(sharedContent) {
        if (sharedContent.isNotBlank() && emailContent.isBlank()) {
            emailContent = sharedContent
        }
    }

    // Naviguer vers le résultat quand il est prêt
    LaunchedEffect(result) {
        if (result != null) {
            onResultReady()
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Analyser un e-mail", color = TextWhite) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📧", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Analyser un e-mail",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Collez le contenu d'un e-mail suspect pour le vérifier",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Champ expéditeur
            OutlinedTextField(
                value = senderEmail,
                onValueChange = { senderEmail = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Adresse de l'expéditeur (optionnel)", color = TextGray) },
                placeholder = { Text("ex: service@exemple.com", color = TextGray.copy(alpha = 0.4f)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f),
                    cursorColor = NeonPurple,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedLabelColor = NeonPurple
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Champ contenu
            OutlinedTextField(
                value = emailContent,
                onValueChange = { emailContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = {
                    Text(
                        "Collez le contenu de l'e-mail ici...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray.copy(alpha = 0.4f)
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                shape = RoundedCornerShape(14.dp),
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
                Text("Analyse en cours...", style = MaterialTheme.typography.bodyLarge, color = NeonPurple)
            } else {
                LargeButton(
                    text = "🔍  Analyser l'e-mail",
                    onClick = {
                        viewModel.analyzeEmail(emailContent, senderEmail.ifBlank { null })
                    },
                    enabled = emailContent.isNotBlank()
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ScamRed.copy(alpha = 0.15f))
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
