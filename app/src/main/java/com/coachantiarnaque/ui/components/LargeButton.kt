package com.coachantiarnaque.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coachantiarnaque.ui.theme.*

/**
 * Bouton principal avec gradient mauve, glow et style moderne.
 */
@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NeonPurple,
    contentColor: Color = Color.Black,
    enabled: Boolean = true
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(PurpleDark, PurplePrimary, PurpleAccent)
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = NeonPurple.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TextWhite,
            disabledContainerColor = DarkCard,
            disabledContentColor = TextGray
        ),
        enabled = enabled,
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (enabled) Modifier.background(gradient, RoundedCornerShape(16.dp))
                    else Modifier.background(DarkCard, RoundedCornerShape(16.dp))
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) TextWhite else TextGray
            )
        }
    }
}

/**
 * Bouton secondaire avec bordure mauve et fond transparent.
 */
@Composable
fun LargeOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        border = ButtonDefaults.outlinedButtonBorder(enabled).copy(
            brush = Brush.horizontalGradient(
                colors = if (enabled) listOf(PurpleDark, NeonPurple)
                else listOf(TextGray.copy(alpha = 0.3f), TextGray.copy(alpha = 0.3f))
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = NeonPurple,
            disabledContentColor = TextGray
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
