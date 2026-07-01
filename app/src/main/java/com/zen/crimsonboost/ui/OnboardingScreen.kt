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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zen.crimsonboost.R
import com.zen.crimsonboost.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardPage(val resId: Int, val title: String, val subtitle: String)

private val pages = listOf(
    OnboardPage(R.drawable.ic_launcher, "SUPERCHARGE\nYOUR GAME", "Kill background processes and free RAM headroom for better FPS in Zalith, PojavLauncher, and Minecraft."),
    OnboardPage(R.drawable.ic_speedometer, "SMOOTHER\nPERFORMANCE", "Stabilize frame rates, reduce lag spikes, and keep your session running at peak performance."),
    OnboardPage(R.drawable.ic_shield, "ZERO\nINTERRUPTIONS", "Silence notifications and calls while you play so nothing breaks your game session.")
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        ArenaBackground()
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Text("CRIMSONBOOST", color = Crimson, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, letterSpacing = 3.sp)
            Spacer(Modifier.height(32.dp))
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val p = pages[page]
                val selected = pagerState.currentPage == page
                val scale by animateFloatAsState(if (selected) 1f else 0.88f, tween(300), label = "scale")
                val alpha by animateFloatAsState(if (selected) 1f else 0.5f, tween(300), label = "alpha")
                Column(
                    modifier = Modifier.fillMaxSize().scale(scale).alpha(alpha),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(160.dp).clip(RoundedCornerShape(36.dp)).background(CardDark), contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Image(painter = painterResource(p.resId), contentDescription = null, modifier = Modifier.size(110.dp))
                    }
                    Spacer(Modifier.height(40.dp))
                    Text(p.title, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, lineHeight = 36.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(p.subtitle, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 21.sp, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 24.dp)) {
                repeat(pages.size) { i ->
                    val selected = pagerState.currentPage == i
                    Box(modifier = Modifier.clip(CircleShape).background(if (selected) Crimson else TextSecondary.copy(alpha = 0.3f)).size(if (selected) 24.dp else 8.dp, 8.dp))
                }
            }
            val isLast = pagerState.currentPage == pages.size - 1
            Button(
                onClick = { if (isLast) onFinish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(if (isLast) "GET STARTED" else "NEXT", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(36.dp))
        }
    }
}
