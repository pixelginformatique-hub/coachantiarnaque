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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coachantiarnaque.R
import com.coachantiarnaque.domain.model.ResultType
import com.coachantiarnaque.ui.theme.*

@Composable
fun ResultCard(resultType: ResultType, modifier: Modifier = Modifier) {
    val (color, label, subtitle) = when (resultType) {
        ResultType.SAFE -> Triple(SafeGreen, stringResource(R.string.result_safe), stringResource(R.string.result_safe_subtitle))
        ResultType.SUSPICIOUS -> Triple(SuspiciousOrange, stringResource(R.string.result_suspicious), stringResource(R.string.result_suspicious_subtitle))
        ResultType.SCAM -> Triple(ScamRed, stringResource(R.string.result_scam), stringResource(R.string.result_scam_subtitle))
    }
    val context = LocalContext.current
    LaunchedEffect(resultType) { if (resultType == ResultType.SCAM) triggerScamVibration(context) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse), label = "glowAlpha")
    val progress by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "borderProgress")

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = color.copy(alpha = glowAlpha * 0.5f), spotColor = color.copy(alpha = glowAlpha))
                .drawWithContent { drawContent(); drawAnimatedBorder(color, 24.dp.toPx(), progress, size) },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Box(Modifier.fillMaxWidth().background(Brush.radialGradient(listOf(color.copy(alpha = 0.15f), Color.Transparent), radius = 400f))) {
                Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    when (resultType) {
                        ResultType.SCAM -> AnimatedSiren()
                        ResultType.SUSPICIOUS -> StaticIcon("⚠️", SuspiciousOrange)
                        ResultType.SAFE -> StaticIcon("🛡️", SafeGreen)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(label, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAnimatedBorder(color: Color, cr: Float, progress: Float, canvasSize: Size) {
    val sw = 3.dp.toPx()
    val w = canvasSize.width; val h = canvasSize.height
    val straightH = (w - 2 * cr).coerceAtLeast(0f); val straightV = (h - 2 * cr).coerceAtLeast(0f)
    val cornerArc = (Math.PI.toFloat() * cr / 2f)
    val totalPerimeter = 2 * straightH + 2 * straightV + 4 * cornerArc

    fun pointAt(dist: Float): Offset {
        var d = ((dist % totalPerimeter) + totalPerimeter) % totalPerimeter
        if (d <= straightH) return Offset(cr + d, 0f); d -= straightH
        if (d <= cornerArc) { val a = -Math.PI / 2.0 + (d / cornerArc) * (Math.PI / 2.0); return Offset(w - cr + cr * Math.cos(a).toFloat(), cr + cr * Math.sin(a).toFloat()) }; d -= cornerArc
        if (d <= straightV) return Offset(w, cr + d); d -= straightV
        if (d <= cornerArc) { val a = 0.0 + (d / cornerArc) * (Math.PI / 2.0); return Offset(w - cr + cr * Math.cos(a).toFloat(), h - cr + cr * Math.sin(a).toFloat()) }; d -= cornerArc
        if (d <= straightH) return Offset(w - cr - d, h); d -= straightH
        if (d <= cornerArc) { val a = Math.PI / 2.0 + (d / cornerArc) * (Math.PI / 2.0); return Offset(cr + cr * Math.cos(a).toFloat(), h - cr + cr * Math.sin(a).toFloat()) }; d -= cornerArc
        if (d <= straightV) return Offset(0f, h - cr - d); d -= straightV
        val a = Math.PI + (d / cornerArc) * (Math.PI / 2.0); return Offset(cr + cr * Math.cos(a).toFloat(), cr + cr * Math.sin(a).toFloat())
    }

    drawRoundRect(color.copy(alpha = 0.1f), cornerRadius = CornerRadius(cr, cr), style = Stroke(sw))
    val headPos = progress * totalPerimeter; val trailLen = totalPerimeter * 0.22f
    for (i in 0 until 60) { val pt = pointAt(headPos - (i.toFloat() / 60) * trailLen); val a = 1f - (i.toFloat() / 60); drawCircle(color.copy(alpha = a * 0.9f), sw * (1f - i.toFloat() / 60 * 0.4f), pt) }
    val head2 = ((progress + 0.5f) % 1f) * totalPerimeter
    for (i in 0 until 30) { val pt = pointAt(head2 - (i.toFloat() / 30) * totalPerimeter * 0.12f); val a = 1f - (i.toFloat() / 30); drawCircle(color.copy(alpha = a * 0.4f), sw * 0.5f, pt) }
}

@Composable private fun AnimatedSiren() {
    val inf = rememberInfiniteTransition(label = "siren")
    val rotation by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "rot")
    val flash by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse), label = "flash")
    val scale by inf.animateFloat(0.95f, 1.1f, infiniteRepeatable(tween(600, easing = EaseInOutCubic), RepeatMode.Reverse), label = "scale")
    val rayAlpha by inf.animateFloat(0.2f, 0.8f, infiniteRepeatable(tween(400, easing = EaseInOutCubic), RepeatMode.Reverse), label = "ray")
    Box(Modifier.size((100 * scale).dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(100.dp).rotate(rotation)) {
            val c = Offset(size.width / 2, size.height / 2); val r = size.width / 2
            for (i in 0 until 8) { val a = Math.toRadians((i * 45.0) + rotation.toDouble()); drawLine(if (i % 2 == 0) Color(0xFFFF1744).copy(alpha = rayAlpha) else Color(0xFF2979FF).copy(alpha = rayAlpha), c, Offset(c.x + (r * Math.cos(a)).toFloat(), c.y + (r * Math.sin(a)).toFloat()), 3f) }
            drawCircle(ScamRed.copy(alpha = rayAlpha * 0.3f), r, c, style = Stroke(2f))
        }
        Box(Modifier.size(80.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(Color(0xFFFF1744).copy(alpha = flash), Color(0xFF2979FF).copy(alpha = 1f - flash)))).border(3.dp, ScamRed.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) { Text("🚨", fontSize = 40.sp) }
    }
}

@Composable private fun StaticIcon(emoji: String, color: Color) {
    Box(Modifier.size(80.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)).border(2.dp, color.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 40.sp) }
}

private fun triggerScamVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager; vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 400), -1)) }
        else { @Suppress("DEPRECATION") val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator; if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 400), -1)) else @Suppress("DEPRECATION") v.vibrate(500) }
    } catch (_: Exception) {}
}

@Composable
fun ResultBadge(resultType: ResultType) {
    val (color, emoji) = when (resultType) { ResultType.SAFE -> Pair(SafeGreen, "🛡️"); ResultType.SUSPICIOUS -> Pair(SuspiciousOrange, "⚠️"); ResultType.SCAM -> Pair(ScamRed, "🚨") }
    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.15f)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
}
