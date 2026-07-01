package com.zen.crimsonboost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zen.crimsonboost.ui.AppIcon
import com.zen.crimsonboost.ui.ArenaBackground
import com.zen.crimsonboost.ui.OnboardingScreen
import com.zen.crimsonboost.ui.SettingsScreen
import com.zen.crimsonboost.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CrimsonBoostTheme { CrimsonBoostRoot() } }
    }
}

private enum class Screen { ONBOARDING, HOME, SETTINGS }

@Composable
private fun CrimsonBoostRoot() {
    val context = LocalContext.current
    val settings = remember { BoostSettings(context) }
    var screen by remember { mutableStateOf(if (settings.onboardingDone) Screen.HOME else Screen.ONBOARDING) }
    when (screen) {
        Screen.ONBOARDING -> OnboardingScreen(onFinish = { settings.onboardingDone = true; screen = Screen.HOME })
        Screen.HOME -> CrimsonBoostApp(settings = settings, onOpenSettings = { screen = Screen.SETTINGS })
        Screen.SETTINGS -> SettingsScreen(onBack = { screen = Screen.HOME })
    }
}

@Composable
fun CrimsonBoostApp(settings: BoostSettings, onOpenSettings: () -> Unit = {}) {
    val context = LocalContext.current
    var targets by remember { mutableStateOf(BoostManager.loadSavedTargets(context)) }
    var showPicker by remember { mutableStateOf(false) }
    var boostingPackage by remember { mutableStateOf<String?>(null) }
    val dndGranted = BoostManager.isDndAccessGranted(context)

    LaunchedEffect(boostingPackage) {
        val pkg = boostingPackage ?: return@LaunchedEffect
        if (settings.killBackgroundApps) BoostManager.killBackgroundApps(context, keepPackage = pkg)
        if (settings.enableDnd && dndGranted) BoostManager.setDnd(context, true)
        delay(750)
        BoostManager.launchApp(context, pkg)
        delay(500)
        boostingPackage = null
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        ArenaBackground()
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(24.dp))
            // Top bar
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(Crimson), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Bolt, null, tint = TextPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("CRIMSONBOOST", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, letterSpacing = 1.5.sp)
                    Text("Game performance booster", color = TextSecondary, fontSize = 11.sp)
                }
                IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, null, tint = TextSecondary) }
            }
            // DND banner
            if (!dndGranted) {
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp))
                    .background(CrimsonDim).clickable { BoostManager.openDndAccessSettings(context) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsOff, null, tint = CrimsonBright, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Grant Do Not Disturb access for silent boosts →", color = CrimsonBright, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(16.dp))
            if (targets.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Bolt, null, tint = CrimsonDim, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No apps added yet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Add Zalith Launcher, PojavLauncher, Minecraft,\nor any app you want to boost.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { showPicker = true }, colors = ButtonDefaults.buttonColors(containerColor = Crimson), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Add App", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(targets, key = { it.packageName }) { target ->
                        BoostCard(target = target, isBoosting = boostingPackage == target.packageName,
                            installed = BoostManager.isPackageInstalled(context, target.packageName),
                            onBoost = { boostingPackage = target.packageName },
                            onRemove = { targets = targets.filterNot { it.packageName == target.packageName }; BoostManager.saveTargets(context, targets) })
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showPicker = true }, containerColor = Crimson, contentColor = TextPrimary, shape = CircleShape, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
            Icon(Icons.Filled.Add, "Add app")
        }
    }

    if (showPicker) {
        AppPickerSheet(context = context, alreadyAdded = targets.map { it.packageName }.toSet(), onDismiss = { showPicker = false },
            onPick = { picked -> targets = (targets + picked).distinctBy { it.packageName }; BoostManager.saveTargets(context, targets); showPicker = false })
    }
}

@Composable
private fun BoostCard(target: BoostTarget, isBoosting: Boolean, installed: Boolean, onBoost: () -> Unit, onRemove: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infinite.animateFloat(0.25f, 0.85f, infiniteRepeatable(tween(750), RepeatMode.Reverse), label = "glow")
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.5.dp, Crimson.copy(alpha = if (isBoosting) glowAlpha else 0.18f), RoundedCornerShape(18.dp)).background(CardDark)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(packageName = target.packageName, size = 54.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(target.label, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(when { !installed -> "Not installed"; isBoosting -> "Boosting…"; else -> "Ready to boost" },
                    color = when { !installed -> TextSecondary.copy(alpha = 0.5f); isBoosting -> CrimsonBright; else -> Success }, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            if (isBoosting) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp, color = CrimsonBright)
            } else {
                if (installed) {
                    Button(onClick = onBoost, colors = ButtonDefaults.buttonColors(containerColor = Crimson), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                        Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("BOOST", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, null, tint = TextSecondary.copy(0.5f), modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerSheet(context: android.content.Context, alreadyAdded: Set<String>, onDismiss: () -> Unit, onPick: (BoostTarget) -> Unit) {
    var query by remember { mutableStateOf("") }
    val allApps = remember { BoostManager.launchableApps(context) }
    val filtered = remember(query) { if (query.isBlank()) allApps else allApps.filter { it.label.contains(query, true) || it.packageName.contains(query, true) } }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SurfaceDark) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxHeight(0.85f)) {
            Text("Add App", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("Search apps", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextSecondary) }, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = Crimson, unfocusedBorderColor = TextSecondary.copy(0.3f), cursorColor = Crimson),
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    val added = app.packageName in alreadyAdded
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (!added) CardDark else CardDark.copy(alpha = 0.5f))
                        .clickable(enabled = !added) { onPick(app) }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(packageName = app.packageName, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.label, color = if (added) TextSecondary else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(app.packageName, color = TextSecondary.copy(0.5f), fontSize = 10.sp)
                        }
                        if (added) Text("Added", color = Success, fontSize = 11.sp) else Icon(Icons.Filled.Add, null, tint = Crimson, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
