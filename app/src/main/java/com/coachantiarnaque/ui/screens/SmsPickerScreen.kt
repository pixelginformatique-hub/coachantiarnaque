package com.coachantiarnaque.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.SmsReader
import com.coachantiarnaque.utils.SmsMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran qui affiche les SMS de la boîte de réception
 * pour que l'utilisateur puisse en sélectionner un à analyser.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsPickerScreen(
    onBack: () -> Unit,
    onSmsSelected: (body: String, sender: String) -> Unit
) {
    val context = LocalContext.current
    val messages = remember { SmsReader.getInboxMessages(context) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Choisir un SMS", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = NeonPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aucun SMS trouvé",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vérifiez que la permission SMS est accordée",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Info en haut
                Text(
                    text = "Appuyez sur un message pour l'analyser",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { sms ->
                        SmsItem(
                            sms = sms,
                            onClick = { onSmsSelected(sms.body, sms.sender) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SmsItem(
    sms: SmsMessage,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM à HH:mm", Locale.FRANCE)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar avec initiale
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = NeonPurple.copy(alpha = 0.15f),
                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(NeonPurple.copy(alpha = 0.3f), NeonPurple.copy(alpha = 0.1f))
                    )
                )
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    val initial = sms.sender.firstOrNull()?.let {
                        if (it.isLetter()) it.uppercase() else "#"
                    } ?: "#"
                    Text(
                        text = initial,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sms.sender,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = dateFormat.format(Date(sms.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = sms.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
