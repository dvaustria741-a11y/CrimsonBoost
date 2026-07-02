package com.zen.crimsonboost.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zen.crimsonboost.BoostManager
import com.zen.crimsonboost.BoostSettings
import com.zen.crimsonboost.BoostTarget
import com.zen.crimsonboost.R
import com.zen.crimsonboost.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(settings: BoostSettings, onOpenSettings: () -> Unit) {
    val context = LocalContext.current

    // ── DND state: re-checks every time the user returns from Settings ────
    var dndGranted by remember { mutableStateOf(BoostManager.isDndAccessGranted(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                dndGranted = BoostManager.isDndAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var targets by remember { mutableStateOf(BoostManager.loadSavedTargets(context)) }
    var showPicker by remember { mutableStateOf(false) }
    var boostingPackage by remember { mutableStateOf<String?>(null) }
    var showBoostOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(boostingPackage) {
        val pkg = boostingPackage ?: return@LaunchedEffect
        showBoostOverlay = true
        if (settings.killBackgroundApps) BoostManager.killBackgroundApps(context, pkg)
        if (settings.enableDnd && dndGranted) BoostManager.setDnd(context, true)
        delay(1500)
        BoostManager.launchApp(context, pkg)
        delay(400)
        showBoostOverlay = false
        boostingPackage = null
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        ArenaBackground()

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(Crimson),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Bolt, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "CRIMSONBOOST", color = TextPrimary, fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic,
                    letterSpacing = 1.5.sp, modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Filled.Add, "Add", tint = TextPrimary)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, "Settings", tint = TextSecondary)
                }
            }

            // ── DND banner — hidden once permission is granted ─────────────
            if (!dndGranted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CrimsonDim)
                        .clickable { BoostManager.openDndAccessSettings(context) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.NotificationsOff, null, tint = CrimsonBright, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Tap to grant Do Not Disturb access for silent boosts →",
                        color = CrimsonBright, fontSize = 12.sp, fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── App cards / empty state ────────────────────────────────────
            if (targets.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Bolt, null, tint = CrimsonDim, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(14.dp))
                        Text("No apps added yet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Add Zalith Launcher, PojavLauncher, Minecraft,\nor any app you want to boost before launch.",
                            color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { showPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add App", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // ── Carousel: each page = full width, card centered inside ──
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val pagerState = rememberPagerState(pageCount = { targets.size })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val rawOffset = ((pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction).absoluteValue
                        val cardScale = lerp(
                            start = 0.82f,
                            stop = 1.00f,
                            fraction = (1f - rawOffset).coerceIn(0f, 1f)
                        )

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.scale(cardScale)) {
                                AppCard(
                                    target = targets[page],
                                    isBoosting = boostingPackage == targets[page].packageName,
                                    installed = BoostManager.isPackageInstalled(context, targets[page].packageName),
                                    onBoost = { boostingPackage = targets[page].packageName },
                                    onRemove = {
                                        val removed = targets[page].packageName
                                        targets = targets.filterNot { it.packageName == removed }
                                        BoostManager.saveTargets(context, targets)
                                    }
                                )
                            }
                        }
                    }

                    // Page dot indicators
                    if (targets.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(targets.size) { i ->
                                val sel = pagerState.currentPage == i
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (sel) Crimson else TextSecondary.copy(0.3f))
                                        .size(if (sel) 18.dp else 6.dp, 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }

        // ── Boost overlay — rendered on top of everything ─────────────────
        AnimatedVisibility(
            visible = showBoostOverlay,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(320))
        ) {
            BoostOverlay()
        }
    }

    if (showPicker) {
        AppPickerGrid(
            alreadyAdded = targets.map { it.packageName }.toSet(),
            onDismiss = { showPicker = false },
            onAdd = { picked ->
                targets = (targets + picked).distinctBy { it.packageName }
                BoostManager.saveTargets(context, targets)
            }
        )
    }
}

// ── Boost overlay ─────────────────────────────────────────────────────────────

@Composable
private fun BoostOverlay() {
    val infinite = rememberInfiniteTransition(label = "bp")
    val pulse by infinite.animateFloat(
        0.92f, 1.06f,
        infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val textAlpha by infinite.animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "ta"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.boost_overlay),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().scale(pulse),
            contentScale = ContentScale.Crop,
            alpha = 0.88f
        )
        Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack.copy(alpha = 0.32f)))
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "BOOSTING",
                color = TextPrimary.copy(alpha = textAlpha),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = 8.sp
            )
            Spacer(Modifier.height(10.dp))
            Text("Clearing background processes…", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

// ── App card ──────────────────────────────────────────────────────────────────

@Composable
private fun AppCard(
    target: BoostTarget,
    isBoosting: Boolean,
    installed: Boolean,
    onBoost: () -> Unit,
    onRemove: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infinite.animateFloat(
        0.25f, 0.9f,
        infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "g"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight(0.88f)
            .width(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.5.dp,
                Crimson.copy(alpha = if (isBoosting) glowAlpha else 0.22f),
                RoundedCornerShape(24.dp)
            )
            .background(Brush.verticalGradient(listOf(CardDark, BackgroundBlack)))
    ) {
        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)
        ) {
            Icon(
                Icons.Filled.Close, null,
                tint = TextSecondary.copy(0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            AppIcon(packageName = target.packageName, size = 96.dp)

            Spacer(Modifier.height(14.dp))

            Text(
                target.label,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when { !installed -> "Not installed"; isBoosting -> "Boosting…"; else -> "Ready to boost" },
                color = when { !installed -> TextSecondary.copy(0.5f); isBoosting -> CrimsonBright; else -> Success },
                fontSize = 12.sp
            )

            Spacer(Modifier.weight(1f))
            if (isBoosting) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.5.dp, color = CrimsonBright)
            } else {
                Button(
                    onClick = onBoost, enabled = installed,
                    colors = ButtonDefaults.buttonColors(containerColor = Crimson, disabledContainerColor = CrimsonDim),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "BOOST",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // HUD frame overlay — transparent crimson border decoration on top
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.card_frame),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alpha = if (isBoosting) 0.9f else 0.6f
        )
    }
}

// ── Grid app picker ───────────────────────────────────────────────────────────

@Composable
private fun AppPickerGrid(alreadyAdded: Set<String>, onDismiss: () -> Unit, onAdd: (List<BoostTarget>) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val allApps = remember { BoostManager.launchableApps(context) }
    val filtered = remember(query) {
        if (query.isBlank()) allApps
        else allApps.filter {
            it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
        }
    }
    val selected = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack.copy(alpha = 0.97f))) {
            Column(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add Apps", color = TextPrimary,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (selected.isNotEmpty()) {
                        Button(
                            onClick = {
                                onAdd(selected.mapNotNull { pkg ->
                                    allApps.find { it.packageName == pkg }
                                })
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("ADD (${selected.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = TextSecondary, fontSize = 13.sp) }
                }

                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Search apps…", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextSecondary) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Crimson,
                        unfocusedBorderColor = TextSecondary.copy(0.3f),
                        cursorColor = Crimson
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 88.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filtered, key = { it.packageName }) { app ->
                        val added = app.packageName in alreadyAdded
                        val isSel = app.packageName in selected
                        GridAppItem(target = app, added = added, selected = isSel) {
                            if (!added) { if (isSel) selected.remove(app.packageName) else selected.add(app.packageName) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridAppItem(
    target: BoostTarget,
    added: Boolean,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (selected) Crimson else TextSecondary.copy(0.08f),
                RoundedCornerShape(14.dp)
            )
            .background(if (selected) CrimsonDim else SurfaceDark)
            .clickable(enabled = !added) { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(6.dp)
        ) {
            Box {
                AppIcon(packageName = target.packageName, size = 52.dp)
                if (selected || added) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (added) TextSecondary else Crimson)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Check, null,
                            tint = TextPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                target.label,
                color = if (added) TextSecondary else TextPrimary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )
        }
    }
}
