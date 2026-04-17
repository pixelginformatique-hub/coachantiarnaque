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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.components.LargeButton
import com.coachantiarnaque.ui.components.LargeOutlinedButton
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.ShareAppUtil
import com.coachantiarnaque.utils.ShareMessageBuilder
import com.coachantiarnaque.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    val lastMessage by viewModel.lastMessage.collectAsStateWithLifecycle()

    // Construire le texte à partager avec le contenu du message
    fun buildShareText(): String {
        val msg = lastMessage
        val typeLabel = msg?.let {
            when (it.resultType) {
                ResultType.SAFE -> "Sécuritaire"
                ResultType.SUSPICIOUS -> "Douteux"
                ResultType.SCAM -> "Arnaque probable"
            }
        }
        return ShareMessageBuilder.forHelpScreen(
            content = msg?.content,
            senderNumber = msg?.senderNumber,
            resultLabel = typeLabel,
            reasons = msg?.reasons
        )
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Aide & Proche", color = TextWhite) },
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
            Text(
                text = "👨‍👩‍👧 Votre personne de confiance",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Envoyez le message suspect à un proche pour avoir son avis.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            // Aperçu du message qui sera partagé
            if (lastMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))

                val msg = lastMessage!!
                val resultColor = when (msg.resultType) {
                    ResultType.SAFE -> SafeGreen
                    ResultType.SUSPICIOUS -> SuspiciousOrange
                    ResultType.SCAM -> ScamRed
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, resultColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = resultColor.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📋 Message qui sera envoyé :",
                            style = MaterialTheme.typography.bodySmall,
                            color = resultColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite,
                            maxLines = 4
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Nom du proche", color = TextGray) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                shape = RoundedCornerShape(12.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Numéro de téléphone", color = TextGray) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                shape = RoundedCornerShape(12.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

            LargeButton(
                text = "📞  Appeler mon proche",
                onClick = {
                    if (contactPhone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$contactPhone")
                        }
                        context.startActivity(intent)
                    }
                },
                enabled = contactPhone.isNotBlank(),
                containerColor = SafeGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            LargeOutlinedButton(
                text = "💬  Envoyer par SMS",
                onClick = {
                    if (contactPhone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:$contactPhone")
                            putExtra("sms_body", buildShareText())
                        }
                        context.startActivity(intent)
                    }
                },
                enabled = contactPhone.isNotBlank()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LargeOutlinedButton(
                text = "📤  Partager (WhatsApp, email...)",
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, buildShareText())
                        putExtra(Intent.EXTRA_SUBJECT, "Coach Anti-Arnaque — Message suspect")
                    }
                    context.startActivity(Intent.createChooser(intent, "Envoyer à..."))
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Conseils de sécurité
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                            text = "💡 Conseils de sécurité",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonPurple
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val tips = listOf(
                        "Ne cliquez jamais sur un lien dans un SMS inattendu",
                        "Votre banque ne demandera jamais votre mot de passe par SMS",
                        "En cas de doute, appelez directement l'organisme concerné",
                        "Ne rappelez jamais un numéro inconnu"
                    )
                    tips.forEach { tip ->
                        Text(
                            text = "• $tip",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton partager l'app
            LargeOutlinedButton(
                text = "💜  Partager l'application à un proche",
                onClick = { ShareAppUtil.shareApp(context) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
