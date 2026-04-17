package com.coachantiarnaque.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.theme.*

@Composable
fun ResultCard(
    resultType: ResultType,
    modifier: Modifier = Modifier
) {
    val (color, label, subtitle) = when (resultType) {
        ResultType.SAFE -> Triple(SafeGreen, "Sécuritaire", "Ce message semble fiable")
        ResultType.SUSPICIOUS -> Triple(SuspiciousOrange, "Attention", "Ce message est douteux")
        ResultType.SCAM -> Triple(ScamRed, "Arnaque probable", "Ne cliquez sur aucun lien")
    }

    val context = LocalContext.current

    LaunchedEffect(resultType) {
        if (resultType == ResultType.SCAM) {
            triggerScamVibration(context)
        }
    }

    // Glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Progress pour la bordure animée
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "borderProgress"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // Bordure animée dessinée directement sur le modifier de la carte
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = color.copy(alpha = glowAlpha * 0.5f),
                    spotColor = color.copy(alpha = glowAlpha)
                )
                .drawWithContent {
                    drawContent()
                    // Dessiner la bordure animée par-dessus
                    drawAnimatedBorder(color, 24.dp.toPx(), progress, size)
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.15f), Color.Transparent),
                            radius = 400f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (resultType) {
                        ResultType.SCAM -> AnimatedSiren()
                        ResultType.SUSPICIOUS -> StaticIcon("⚠️", SuspiciousOrange)
                        ResultType.SAFE -> StaticIcon("🛡️", SafeGreen)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Pas besoin d'overlay séparé, la bordure est dessinée via drawWithContent
    }
}

/**
 * Dessine une bordure animée directement dans le DrawScope de la carte.
 * Le trait lumineux parcourt tout le contour rectangulaire arrondi.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAnimatedBorder(
    color: Color,
    cr: Float,
    progress: Float,
    canvasSize: Size
) {
    val sw = 3.dp.toPx()
    val w = canvasSize.width
    val h = canvasSize.height

    // Périmètre : 4 segments droits + 4 quarts de cercle
    val straightH = (w - 2 * cr).coerceAtLeast(0f)
    val straightV = (h - 2 * cr).coerceAtLeast(0f)
    val cornerArc = (Math.PI.toFloat() * cr / 2f)
    val totalPerimeter = 2 * straightH + 2 * straightV + 4 * cornerArc

    fun pointAt(dist: Float): Offset {
        var d = ((dist % totalPerimeter) + totalPerimeter) % totalPerimeter

        // Haut : gauche → droite
        if (d <= straightH) {
            return Offset(cr + d, 0f)
        }
        d -= straightH

        // Coin haut-droit
        if (d <= cornerArc) {
            val a = -Math.PI / 2.0 + (d / cornerArc) * (Math.PI / 2.0)
            return Offset(
                w - cr + cr * Math.cos(a).toFloat(),
                cr + cr * Math.sin(a).toFloat()
            )
        }
        d -= cornerArc

        // Droite : haut → bas
        if (d <= straightV) {
            return Offset(w, cr + d)
        }
        d -= straightV

        // Coin bas-droit
        if (d <= cornerArc) {
            val a = 0.0 + (d / cornerArc) * (Math.PI / 2.0)
            return Offset(
                w - cr + cr * Math.cos(a).toFloat(),
                h - cr + cr * Math.sin(a).toFloat()
            )
        }
        d -= cornerArc

        // Bas : droite → gauche
        if (d <= straightH) {
            return Offset(w - cr - d, h)
        }
        d -= straightH

        // Coin bas-gauche
        if (d <= cornerArc) {
            val a = Math.PI / 2.0 + (d / cornerArc) * (Math.PI / 2.0)
            return Offset(
                cr + cr * Math.cos(a).toFloat(),
                h - cr + cr * Math.sin(a).toFloat()
            )
        }
        d -= cornerArc

        // Gauche : bas → haut
        if (d <= straightV) {
            return Offset(0f, h - cr - d)
        }
        d -= straightV

        // Coin haut-gauche
        val a = Math.PI + (d / cornerArc) * (Math.PI / 2.0)
        return Offset(
            cr + cr * Math.cos(a).toFloat(),
            cr + cr * Math.sin(a).toFloat()
        )
    }

    // Contour subtil de base
    drawRoundRect(
        color = color.copy(alpha = 0.1f),
        cornerRadius = CornerRadius(cr, cr),
        style = Stroke(width = sw)
    )

    // Trait lumineux principal
    val headPos = progress * totalPerimeter
    val trailLen = totalPerimeter * 0.22f
    val numDots = 60
    for (i in 0 until numDots) {
        val dotDist = headPos - (i.toFloat() / numDots) * trailLen
        val pt = pointAt(dotDist)
        val a = 1f - (i.toFloat() / numDots)
        drawCircle(
            color = color.copy(alpha = a * 0.9f),
            radius = sw * (1f - i.toFloat() / numDots * 0.4f),
            center = pt
        )
    }

    // Deuxième trait à l'opposé
    val head2 = ((progress + 0.5f) % 1f) * totalPerimeter
    val trail2 = totalPerimeter * 0.12f
    val numDots2 = 30
    for (i in 0 until numDots2) {
        val dotDist = head2 - (i.toFloat() / numDots2) * trail2
        val pt = pointAt(dotDist)
        val a = 1f - (i.toFloat() / numDots2)
        drawCircle(
            color = color.copy(alpha = a * 0.4f),
            radius = sw * 0.5f,
            center = pt
        )
    }
}

@Composable
private fun AnimatedSiren() {
    val infiniteTransition = rememberInfiniteTransition(label = "siren")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val flashProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rayAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rayAlpha"
    )

    val flashColorLeft = Color(0xFFFF1744).copy(alpha = flashProgress)
    val flashColorRight = Color(0xFF2979FF).copy(alpha = 1f - flashProgress)

    Box(
        modifier = Modifier.size((100 * scale).dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .rotate(rotation)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0) + rotation.toDouble())
                val endX = center.x + (radius * Math.cos(angle)).toFloat()
                val endY = center.y + (radius * Math.sin(angle)).toFloat()
                drawLine(
                    color = if (i % 2 == 0) Color(0xFFFF1744).copy(alpha = rayAlpha)
                    else Color(0xFF2979FF).copy(alpha = rayAlpha),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3f
                )
            }

            drawCircle(
                color = ScamRed.copy(alpha = rayAlpha * 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(flashColorLeft, flashColorRight)
                    )
                )
                .border(3.dp, ScamRed.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🚨", fontSize = 40.sp)
        }
    }
}

@Composable
private fun StaticIcon(emoji: String, color: Color) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .border(2.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 40.sp)
    }
}

private fun triggerScamVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            val pattern = longArrayOf(0, 200, 100, 200, 100, 400)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 200, 100, 200, 100, 400)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    } catch (_: Exception) { }
}

@Composable
fun ResultBadge(resultType: ResultType) {
    val (color, emoji) = when (resultType) {
        ResultType.SAFE -> Pair(SafeGreen, "🛡️")
        ResultType.SUSPICIOUS -> Pair(SuspiciousOrange, "⚠️")
        ResultType.SCAM -> Pair(ScamRed, "🚨")
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}
