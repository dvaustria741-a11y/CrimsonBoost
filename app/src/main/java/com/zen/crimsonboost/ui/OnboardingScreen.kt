package com.zen.crimsonboost.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zen.crimsonboost.R
import com.zen.crimsonboost.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardPage(val resId: Int, val title: String, val subtitle: String)

private val pages = listOf(
    OnboardPage(R.drawable.ic_launcher,    "SUPERCHARGE\nYOUR GAME",      "Kill background processes and free RAM headroom for better FPS in Zalith, PojavLauncher, and Minecraft."),
    OnboardPage(R.drawable.ic_speedometer, "SMOOTHER\nPERFORMANCE",       "Stabilize frame rates, reduce lag spikes, and keep your session running at peak performance."),
    OnboardPage(R.drawable.ic_shield,      "ZERO\nINTERRUPTIONS",         "Silence notifications and calls while you play so nothing breaks your game session.")
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        ArenaBackground()

        Column(modifier = Modifier.fillMaxSize()) {

            // Brand title
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "CRIMSONBOOST",
                    color = Crimson,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 3.sp
                )
            }

            // Pager — takes remaining vertical space
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val p = pages[page]
                val selected = pagerState.currentPage == page
                val scale by animateFloatAsState(if (selected) 1f else 0.92f, tween(280), label = "s")
                val alpha by animateFloatAsState(if (selected) 1f else 0.45f, tween(280), label = "a")

                // ── Landscape 2-column layout ──────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .alpha(alpha)
                        .padding(horizontal = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(48.dp)
                ) {
                    // Left: icon
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(CardDark),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(p.resId),
                            contentDescription = null,
                            modifier = Modifier.size(105.dp)
                        )
                    }

                    // Right: text
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            p.title,
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 28.sp
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            p.subtitle,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Bottom bar: indicators left, button right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(pages.size) { i ->
                        val sel = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (sel) Crimson else TextSecondary.copy(alpha = 0.3f))
                                .size(if (sel) 22.dp else 7.dp, 7.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                val isLast = pagerState.currentPage == pages.size - 1
                Button(
                    onClick = {
                        if (isLast) onFinish()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(190.dp).height(44.dp)
                ) {
                    Text(
                        if (isLast) "GET STARTED" else "NEXT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
