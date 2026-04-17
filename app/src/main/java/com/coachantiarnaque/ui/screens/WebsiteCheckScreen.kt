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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coachantiarnaque.domain.engine.RiskLevel
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.components.LargeOutlinedButton
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.ShareMessageBuilder
import com.coachantiarnaque.viewmodel.WebsiteCheckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteCheckScreen(
    onBack: () -> Unit,
    onAskContact: () -> Unit,
    viewModel: WebsiteCheckViewModel = viewModel()
) {
    var urlText by remember { mutableStateOf("") }
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val analyzedUrl by viewModel.analyzedUrl.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Vérifier un site", color = TextWhite) },
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
            // Titre
            Text(
                text = "🌐",
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vérifier un site ou une boutique",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Collez le lien pour vérifier s'il présente des signes de risque",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Champ URL
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Collez le lien du site ici",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextGray.copy(alpha = 0.5f)
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = NeonPurple.copy(alpha = 0.2f),
                    cursorColor = NeonPurple,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bouton analyser
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = NeonPurple,
                    trackColor = DarkCard
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Vérification en cours...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonPurple
                )
            } else {
                LargeButton(
                    text = "🔍  Analyser le site",
                    onClick = {
                        viewModel.analyzeWebsite(urlText)
                    },
                    enabled = urlText.isNotBlank()
                )
            }

            // Erreur
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

            // Résultat
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                result?.let { res ->
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))

                        // Carte résultat
                        WebsiteResultCard(res)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Raisons
                        SectionHeader("Détails de l'analyse")
                        Spacer(modifier = Modifier.height(8.dp))

                        val reasonColor = when (res.riskLevel) {
                            RiskLevel.LOW -> SafeGreen
                            RiskLevel.MODERATE -> SuspiciousOrange
                            RiskLevel.HIGH -> ScamRed
                        }

                        res.reasons.forEach { reason ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, reasonColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = reasonColor.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = when (res.riskLevel) {
                                            RiskLevel.LOW -> "✅"
                                            RiskLevel.MODERATE -> "⚠️"
                                            RiskLevel.HIGH -> "🚨"
                                        },
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextWhite
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Boutons d'action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val fullUrl = if (!analyzedUrl.startsWith("http"))
                                        "https://$analyzedUrl" else analyzedUrl
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))
                                    )
                                )
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ouvrir", color = NeonPurple, style = MaterialTheme.typography.bodySmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    val levelLabel = when (res.riskLevel) {
                                        RiskLevel.LOW -> "Faible risque"
                                        RiskLevel.MODERATE -> "Risque modéré"
                                        RiskLevel.HIGH -> "Risque élevé"
                                    }
                                    val shareText = ShareMessageBuilder.forWebsite(
                                        url = analyzedUrl,
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
                                    brush = Brush.horizontalGradient(
                                        listOf(NeonPurple.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))
                                    )
                                )
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Demander avis", color = NeonPurple, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WebsiteResultCard(result: com.coachantiarnaque.domain.engine.WebsiteAnalysisResult) {
    val (color, emoji, label, message) = when (result.riskLevel) {
        RiskLevel.LOW -> ResultInfo(
            SafeGreen, "🟢", "Faible risque",
            "Ce site présente peu de signes de risque"
        )
        RiskLevel.MODERATE -> ResultInfo(
            SuspiciousOrange, "🟠", "Risque modéré",
            "Ce site présente certains signes de risque"
        )
        RiskLevel.HIGH -> ResultInfo(
            ScamRed, "🔴", "Risque élevé",
            "Ce site présente plusieurs signes de risque. Soyez prudent"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.verticalGradient(listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.1f))),
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.12f), Color.Transparent),
                        radius = 400f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cercle avec emoji
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f))
                        .border(2.dp, color.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Domaine analysé
                Text(
                    text = result.domain,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonPurple.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
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
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class ResultInfo(val color: Color, val emoji: String, val label: String, val message: String)
