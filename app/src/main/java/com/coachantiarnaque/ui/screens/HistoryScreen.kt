package com.coachantiarnaque.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coachantiarnaque.R
import com.coachantiarnaque.ui.components.ResultBadge
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(onBack: () -> Unit, onMessageClick: (Long) -> Unit, viewModel: HistoryViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var longPressedId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = DarkBg,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.history_title), color = TextWhite) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NeonPurple) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)) }
    ) { padding ->
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("📭", fontSize = 40.sp); Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.no_history), style = MaterialTheme.typography.bodyLarge, color = TextGray) } }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages, key = { it.id }) { message ->
                    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    val isLongPressed = longPressedId == message.id
                    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { dv -> if (dv == SwipeToDismissBoxValue.EndToStart || dv == SwipeToDismissBoxValue.StartToEnd) { viewModel.deleteMessage(message.id); true } else false })
                    SwipeToDismissBox(state = dismissState, backgroundContent = {
                        val bgColor by animateColorAsState(if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) ScamRed.copy(alpha = 0.3f) else DarkCard, label = "bg")
                        val iconScale by animateFloatAsState(if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 0.8f, label = "ic")
                        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(bgColor).padding(horizontal = 24.dp), contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd) { Icon(Icons.Default.Delete, null, tint = ScamRed, modifier = Modifier.scale(iconScale).size(28.dp)) }
                    }) {
                        Card(Modifier.fillMaxWidth().combinedClickable(onClick = { if (isLongPressed) longPressedId = null else onMessageClick(message.id) }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); longPressedId = if (isLongPressed) null else message.id }).border(1.dp, if (isLongPressed) ScamRed.copy(alpha = 0.4f) else NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isLongPressed) ScamRed.copy(alpha = 0.08f) else DarkCard)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                ResultBadge(message.resultType); Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) { Text(message.content, style = MaterialTheme.typography.bodyMedium, color = TextWhite, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(4.dp)); Text(dateFormat.format(Date(message.timestamp)), style = MaterialTheme.typography.bodySmall, color = TextGray) }
                                if (isLongPressed) { IconButton(onClick = { viewModel.deleteMessage(message.id); longPressedId = null }) { Icon(Icons.Default.Delete, null, tint = ScamRed, modifier = Modifier.size(26.dp)) } }
                                else { Icon(Icons.Default.ChevronRight, null, tint = NeonPurple.copy(alpha = 0.5f)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
