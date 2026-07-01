package com.zen.crimsonboost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.zen.crimsonboost.ui.HomeScreen
import com.zen.crimsonboost.ui.OnboardingScreen
import com.zen.crimsonboost.ui.SettingsScreen
import com.zen.crimsonboost.ui.theme.CrimsonBoostTheme

private enum class Screen { ONBOARDING, HOME, SETTINGS }

class MainActivity : ComponentActivity() {

    private fun goImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goImmersive()

        val boostSettings = BoostSettings(this)
        setContent {
            CrimsonBoostTheme {
                var screen by remember {
                    mutableStateOf(
                        if (boostSettings.onboardingDone) Screen.HOME else Screen.ONBOARDING
                    )
                }
                when (screen) {
                    Screen.ONBOARDING -> OnboardingScreen {
                        boostSettings.onboardingDone = true
                        screen = Screen.HOME
                    }
                    Screen.HOME -> HomeScreen(
                        settings = boostSettings,
                        onOpenSettings = { screen = Screen.SETTINGS }
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        settings = boostSettings,
                        onBack = { screen = Screen.HOME }
                    )
                }
            }
        }
    }

    // Re-hide system bars when focus returns (e.g. after swipe-reveal or rotation)
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) goImmersive()
    }
}
