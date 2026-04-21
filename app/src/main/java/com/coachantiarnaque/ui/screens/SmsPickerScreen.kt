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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coachantiarnaque.R
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.utils.SmsReader
import com.coachantiarnaque.utils.SmsMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsPickerScreen(onBack: () -> Unit, onSmsSelected: (body: String, sender: String) -> Unit) {
    val context = LocalContext.current
    val messages = remember { SmsReader.getInboxMessages(context) }

    Scaffold(
        containerColor = DarkBg,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.pick_sms_title), color = TextWhite) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NeonPurple) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)) }
    ) { padding ->
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 40.sp); Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.no_sms_found), style = MaterialTheme.typography.titleMedium, color = TextWhite)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.check_sms_permission), style = MaterialTheme.typography.bodyMedium, color = TextGray)
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                Text(stringResource(R.string.pick_sms_hint), style = MaterialTheme.typography.bodyMedium, color = TextGray, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(messages, key = { it.id }) { sms ->
                        val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                        Card(Modifier.fillMaxWidth().clickable { onSmsSelected(sms.body, sms.sender) }.border(1.dp, NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
                                Surface(Modifier.size(44.dp), shape = CircleShape, color = NeonPurple.copy(alpha = 0.15f)) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(sms.sender.firstOrNull()?.let { if (it.isLetter()) it.uppercase() else "#" } ?: "#", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(sms.sender, style = MaterialTheme.typography.bodyMedium, color = TextWhite, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)); Text(dateFormat.format(Date(sms.date)), style = MaterialTheme.typography.bodySmall, color = TextGray) }
                                    Spacer(Modifier.height(4.dp))
                                    Text(sms.body, style = MaterialTheme.typography.bodySmall, color = TextGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
