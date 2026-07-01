package com.zen.crimsonboost.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.zen.crimsonboost.ui.theme.*

@Composable
fun ArenaBackground(modifier: Modifier = Modifier) {
    val crimson = Crimson
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val apex = Offset(w / 2f, h * 0.30f)
        val leftLines = 10
        repeat(leftLines) { i ->
            val t = i.toFloat() / (leftLines - 1)
            val alpha = lerp(0.45f, 0.02f, t)
            val stroke = lerp(2.5f, 0.5f, t)
            drawLine(color = crimson.copy(alpha = alpha), start = Offset(lerp(0f, w / 2f, t), h), end = apex, strokeWidth = stroke)
        }
        val rightLines = 10
        repeat(rightLines) { i ->
            val t = i.toFloat() / (rightLines - 1)
            val alpha = lerp(0.02f, 0.45f, t)
            val stroke = lerp(0.5f, 2.5f, t)
            drawLine(color = crimson.copy(alpha = alpha), start = Offset(lerp(w / 2f, w, t), h), end = apex, strokeWidth = stroke)
        }
        drawCircle(
            brush = Brush.radialGradient(listOf(crimson.copy(alpha = 0.25f), Color.Transparent), center = apex, radius = 220f),
            radius = 220f, center = apex
        )
        drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, crimson.copy(alpha = 0.06f)), startY = h * 0.75f, endY = h))
    }
}

@Composable
fun AppIcon(packageName: String, size: Dp = 50.dp) {
    val context = LocalContext.current
    val bitmap: ImageBitmap? = remember(packageName) {
        runCatching {
            val d = context.packageManager.getApplicationIcon(packageName)
            val bmp = Bitmap.createBitmap(d.intrinsicWidth.coerceAtLeast(64), d.intrinsicHeight.coerceAtLeast(64), Bitmap.Config.ARGB_8888)
            val c = android.graphics.Canvas(bmp)
            d.setBounds(0, 0, c.width, c.height); d.draw(c)
            bmp.asImageBitmap()
        }.getOrNull()
    }
    Box(modifier = Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(SurfaceDark), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                painter = BitmapPainter(bitmap), contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(CrimsonDim), contentAlignment = Alignment.Center) {
                Text("?", color = CrimsonBright, fontSize = (size.value * 0.4f).sp)
            }
        }
    }
}

@Composable
fun SettingToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(if (checked) CrimsonDim else SurfaceDark), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = if (checked) CrimsonBright else TextSecondary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Spacer(Modifier.width(10.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = Crimson, uncheckedThumbColor = TextSecondary, uncheckedTrackColor = SurfaceDark))
    }
}
