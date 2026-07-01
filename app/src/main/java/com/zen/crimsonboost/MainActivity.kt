package com.zen.crimsonboost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zen.crimsonboost.ui.OnboardingScreen
import com.zen.crimsonboost.ui.SettingsScreen
import com.zen.crimsonboost.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrimsonBoostTheme {
                CrimsonBoostRoot()
            }
        }
    }
}

private enum class Screen { ONBOARDING, HOME, SETTINGS }

@Composable
private fun CrimsonBoostRoot() {
    val context = LocalContext.current
    val settings = remember { BoostSettings(context) }
    var screen by remember {
        mutableStateOf(if (settings.onboardingDone) Screen.HOME else Screen.ONBOARDING)
    }

    when (screen) {
        Screen.ONBOARDING -> OnboardingScreen(onFinish = {
            settings.onboardingDone = true
            screen = Screen.HOME
        })
        Screen.HOME -> CrimsonBoostApp(onOpenSettings = { screen = Screen.SETTINGS })
        Screen.SETTINGS -> SettingsScreen(onBack = { screen = Screen.HOME })
    }
}

@Composable
fun CrimsonBoostApp(onOpenSettings: () -> Unit = {}) {
    val context = LocalContext.current
    var targets by remember { mutableStateOf(BoostManager.loadSavedTargets(context)) }
    var showPicker by remember { mutableStateOf(false) }
    var boostingPackage by remember { mutableStateOf<String?>(null) }
    var dndGranted by remember { mutableStateOf(BoostManager.isDndAccessGranted(context)) }

    fun runBoost(target: BoostTarget) {
        boostingPackage = target.packageName
    }

    LaunchedEffect(boostingPackage) {
        val pkg = boostingPackage ?: return@LaunchedEffect
        BoostManager.killBackgroundApps(context, keepPackage = pkg)
        if (dndGranted) BoostManager.setDnd(context, true)
        delay(650)
        BoostManager.launchApp(context, pkg)
        delay(400)
        boostingPackage = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BackgroundBlack, SurfaceDark, BackgroundBlack)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(28.dp))
            Header(
                dndGranted = dndGranted,
                onGrantDnd = { BoostManager.openDndAccessSettings(context) },
                onOpenSettings = onOpenSettings
            )
            Spacer(Modifier.height(24.dp))

            if (targets.isEmpty()) {
                EmptyState(onAdd = { showPicker = true })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(targets, key = { it.packageName }) { target ->
                        BoostCard(
                            target = target,
                            isBoosting = boostingPackage == target.packageName,
                            installed = BoostManager.isPackageInstalled(context, target.packageName),
                            onBoost = { runBoost(target) },
                            onRemove = {
                                targets = targets.filterNot { it.packageName == target.packageName }
                                BoostManager.saveTargets(context, targets)
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showPicker = true },
            containerColor = Crimson,
            contentColor = TextPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add app")
        }
    }

    if (showPicker) {
        AppPickerSheet(
            context = context,
            alreadyAdded = targets.map { it.packageName }.toSet(),
            onDismiss = { showPicker = false },
            onPick = { picked ->
                targets = (targets + picked).distinctBy { it.packageName }
                BoostManager.saveTargets(context, targets)
                showPicker = false
            }
        )
    }
}

@Composable
private fun Header(dndGranted: Boolean, onGrantDnd: () -> Unit, onOpenSettings: () -> Unit = {}) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Crimson, CrimsonBright))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = TextPrimary)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("CrimsonBoost", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Game performance booster", color = TextSecondary, fontSize = 12.sp)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextSecondary)
            }
        }
        if (!dndGranted) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CrimsonDim)
                    .clickable { onGrantDnd() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Grant Do-Not-Disturb access for silent boosts →",
                    color = CrimsonBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Bolt, contentDescription = null, tint = CrimsonDim, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("No apps added yet", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Add Zalith Launcher, PojavLauncher, Minecraft,\nor any app you want to boost before launch.",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = Crimson)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Add app")
        }
    }
}

@Composable
private fun BoostCard(
    target: BoostTarget,
    isBoosting: Boolean,
    installed: Boolean,
    onBoost: () -> Unit,
    onRemove: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "glow")
    val glow by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (isBoosting) Crimson.copy(alpha = glow) else SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Bolt,
                contentDescription = null,
                tint = if (isBoosting) TextPrimary else CrimsonBright
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(target.label, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(
                if (!installed) "Not installed" else if (isBoosting) "Boosting…" else "Ready",
                color = if (!installed) TextSecondary.copy(alpha = 0.6f) else if (isBoosting) CrimsonBright else Success,
                fontSize = 12.sp
            )
        }
        if (isBoosting) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = CrimsonBright
            )
        } else {
            IconButton(onClick = onBoost, enabled = installed) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Boost", tint = if (installed) Crimson else TextSecondary.copy(alpha = 0.4f))
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerSheet(
    context: android.content.Context,
    alreadyAdded: Set<String>,
    onDismiss: () -> Unit,
    onPick: (BoostTarget) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allApps = remember { BoostManager.launchableApps(context) }
    val filtered = remember(query) {
        if (query.isBlank()) allApps
        else allApps.filter {
            it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxHeight(0.85f)) {
            Text("Add app to boost", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search installed apps", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Crimson,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    cursorColor = Crimson
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    val added = app.packageName in alreadyAdded
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !added) { onPick(app) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.label, color = TextPrimary, fontSize = 14.sp)
                            Text(app.packageName, color = TextSecondary, fontSize = 11.sp)
                        }
                        if (added) {
                            Text("Added", color = Success, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
