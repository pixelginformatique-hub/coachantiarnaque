package com.coachantiarnaque.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coachantiarnaque.ui.components.ResultBadge
import com.coachantiarnaque.ui.theme.*
import com.coachantiarnaque.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onMessageClick: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Track quel item est en mode "long press" (affiche poubelle)
    var longPressedId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Historique", color = TextWhite) },
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
                        text = "Aucun message analysé",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE)
                    val isLongPressed = longPressedId == message.id

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart ||
                                dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                viewModel.deleteMessage(message.id)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            // Fond rouge avec icône poubelle lors du swipe
                            val direction = dismissState.dismissDirection
                            val bgColor by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.Settled -> DarkCard
                                    else -> ScamRed.copy(alpha = 0.3f)
                                },
                                label = "bgColor"
                            )
                            val iconScale by animateFloatAsState(
                                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 0.8f,
                                label = "iconScale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bgColor)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd)
                                    Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = ScamRed,
                                    modifier = Modifier.scale(iconScale).size(28.dp)
                                )
                            }
                        },
                        content = {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (isLongPressed) {
                                                longPressedId = null
                                            } else {
                                                onMessageClick(message.id)
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            longPressedId = if (isLongPressed) null else message.id
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isLongPressed) ScamRed.copy(alpha = 0.4f)
                                        else NeonPurple.copy(alpha = 0.1f),
                                        RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isLongPressed) ScamRed.copy(alpha = 0.08f) else DarkCard
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ResultBadge(resultType = message.resultType)

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = message.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextWhite,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = dateFormat.format(Date(message.timestamp)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextGray
                                        )
                                    }

                                    if (isLongPressed) {
                                        // Icône poubelle après long press
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteMessage(message.id)
                                                longPressedId = null
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Supprimer",
                                                tint = ScamRed,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = NeonPurple.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
