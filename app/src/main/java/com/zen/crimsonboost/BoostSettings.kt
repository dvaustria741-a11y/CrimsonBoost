package com.zen.crimsonboost

import android.content.Context
import android.provider.Settings as AndroidSettings

class BoostSettings(private val context: Context) {
    private val prefs = context.getSharedPreferences("boost_settings", Context.MODE_PRIVATE)
    var killBackgroundApps: Boolean
        get() = prefs.getBoolean("kill_bg", true)
        set(v) = prefs.edit().putBoolean("kill_bg", v).apply()
    var limitBackgroundData: Boolean
        get() = prefs.getBoolean("limit_bg_data", false)
        set(v) = prefs.edit().putBoolean("limit_bg_data", v).apply()
    var disableAutoBrightness: Boolean
        get() = prefs.getBoolean("disable_auto_brightness", true)
        set(v) = prefs.edit().putBoolean("disable_auto_brightness", v).apply()
    var enableDnd: Boolean
        get() = prefs.getBoolean("enable_dnd", true)
        set(v) = prefs.edit().putBoolean("enable_dnd", v).apply()
    var hideNotifications: Boolean
        get() = prefs.getBoolean("hide_notifs", true)
        set(v) = prefs.edit().putBoolean("hide_notifs", v).apply()
    var boostOnLaunch: Boolean
        get() = prefs.getBoolean("boost_on_launch", true)
        set(v) = prefs.edit().putBoolean("boost_on_launch", v).apply()
    var onboardingDone: Boolean
        get() = prefs.getBoolean("onboarding_done", false)
        set(v) = prefs.edit().putBoolean("onboarding_done", v).apply()
    fun canWriteSettings(): Boolean = AndroidSettings.System.canWrite(context)
}
