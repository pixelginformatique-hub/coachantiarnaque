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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun AnalysisDetailScreen(
    messageId: Long,
    onBack: () -> Unit,
    viewModel: AnalysisViewModel = viewModel()
) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(messageId) {
        viewModel.loadMessage(messageId)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Résultat de l'analyse", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = NeonPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        val msg = message
        if (msg == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonPurple)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ResultCard(resultType = msg.resultType)

            Spacer(modifier = Modifier.height(20.dp))

            // Section message
            SectionHeader("Message analysé")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Text(
                    text = msg.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextWhite,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            val dateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
            Text(
                text = "Analysé le ${dateFormat.format(Date(msg.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Raisons
            SectionHeader("Pourquoi ce résultat")
            Spacer(modifier = Modifier.height(8.dp))

            msg.reasons.forEach { reason ->
                val reasonColor = when (msg.resultType) {
                    ResultType.SAFE -> SafeGreen
                    ResultType.SUSPICIOUS -> SuspiciousOrange
                    ResultType.SCAM -> ScamRed
                }
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
                            text = when (msg.resultType) {
                                ResultType.SAFE -> "✅"
                                ResultType.SUSPICIOUS -> "⚠️"
                                ResultType.SCAM -> "🚨"
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

            Spacer(modifier = Modifier.height(32.dp))

            LargeButton(
                text = "📤  Envoyer à un proche",
                onClick = {
                    val typeLabel = when (msg.resultType) {
                        ResultType.SAFE -> "Sécuritaire"
                        ResultType.SUSPICIOUS -> "Douteux"
                        ResultType.SCAM -> "Arnaque probable"
                    }
                    val shareText = ShareMessageBuilder.forSms(
                        content = msg.content,
                        senderNumber = msg.senderNumber,
                        resultLabel = typeLabel,
                        reasons = msg.reasons
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Envoyer à..."))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LargeOutlinedButton(
                text = "🗑️  Supprimer ce message",
                onClick = { viewModel.deleteMessage(msg.id) { onBack() } }
            )

            Spacer(modifier = Modifier.height(24.dp))
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
