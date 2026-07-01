package com.zen.crimsonboost.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zen.crimsonboost.BoostSettings
import com.zen.crimsonboost.ShizukuHelper
import com.zen.crimsonboost.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { BoostSettings(context) }
    var killBg by remember { mutableStateOf(settings.killBackgroundApps) }
    var limitData by remember { mutableStateOf(settings.limitBackgroundData) }
    var autoBright by remember { mutableStateOf(settings.disableAutoBrightness) }
    var dnd by remember { mutableStateOf(settings.enableDnd) }
    var hideNotifs by remember { mutableStateOf(settings.hideNotifications) }
    var boostOnLaunch by remember { mutableStateOf(settings.boostOnLaunch) }
    var shizukuGranted by remember { mutableStateOf(ShizukuHelper.hasPermission()) }
    val shizukuInstalled = remember { ShizukuHelper.isShizukuInstalled(context) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        ArenaBackground()
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(28.dp))
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary) }
                Spacer(Modifier.width(4.dp))
                Text("SETTINGS", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, letterSpacing = 2.sp)
            }
            HorizontalDivider(color = Crimson.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp, bottom = 60.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (shizukuInstalled && !shizukuGranted) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CrimsonDim)
                            .clickable { ShizukuHelper.requestPermission { shizukuGranted = it } }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bolt, null, tint = CrimsonBright, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Connect Shizuku for deeper tweaks", color = CrimsonBright, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("No root needed — unlocks system-level boosts. Tap to grant.", color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }
                } else if (!shizukuInstalled) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceDark).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Install Shizuku to unlock deeper optimizations without root.", color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                }
                item { SectionLabel("Optimization") }
                item { SettingToggleRow(Icons.Filled.Speed, "Kill background apps", "Clean background processes and increase CPU headroom before boosting.", killBg) { killBg = it; settings.killBackgroundApps = it } }
                item { SettingToggleRow(Icons.Filled.Memory, "Limit background data", "Reduce background download speed while a boosted app is running.", limitData) { limitData = it; settings.limitBackgroundData = it } }
                item { SectionLabel("Enhancement") }
                item { SettingToggleRow(Icons.Filled.BrightnessMedium, "Disable auto brightness", "Keep brightness steady during games instead of auto-adjusting.", autoBright) { autoBright = it; settings.disableAutoBrightness = it } }
                item { SectionLabel("Game DND") }
                item { SettingToggleRow(Icons.Filled.DoNotDisturbOn, "Enable Do Not Disturb", "Silence calls and alerts while a boosted app is in the foreground.", dnd) { dnd = it; settings.enableDnd = it } }
                item { SettingToggleRow(Icons.Filled.NotificationsOff, "Hide notifications", "Suppress heads-up notifications so nothing interrupts your session.", hideNotifs) { hideNotifs = it; settings.hideNotifications = it } }
                item { SectionLabel("General") }
                item { SettingToggleRow(Icons.Filled.Bolt, "Boost on launch", "Automatically run a boost pass as soon as CrimsonBoost opens.", boostOnLaunch) { boostOnLaunch = it; settings.boostOnLaunch = it } }
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceDark).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("CrimsonBoost", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Version 1.0.0 · by Zen", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), color = CrimsonBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
}
