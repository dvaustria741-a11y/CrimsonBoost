package com.zen.crimsonboost

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

data class BoostTarget(
    val label: String,
    val packageName: String
)

object BoostManager {

    private const val PREFS = "crimson_boost_prefs"
    private const val KEY_TARGETS = "targets"

    // Packages CrimsonBoost will never kill during a boost pass.
    private val PROTECTED_PREFIXES = listOf(
        "com.zen.crimsonboost",
        "com.android",
        "android",
        "com.miui",
        "com.xiaomi",
        "com.google.android.inputmethod",
        "com.google.android.gms"
    )

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getAppLabel(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun launchableApps(context: Context): List<BoostTarget> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(intent, 0)
        return resolved
            .map { BoostTarget(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun loadSavedTargets(context: Context): List<BoostTarget> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getStringSet(KEY_TARGETS, emptySet()) ?: emptySet()
        return raw.mapNotNull { entry ->
            val parts = entry.split("|", limit = 2)
            if (parts.size == 2) BoostTarget(parts[0], parts[1]) else null
        }.sortedBy { it.label.lowercase() }
    }

    fun saveTargets(context: Context, targets: List<BoostTarget>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = targets.map { "${it.label}|${it.packageName}" }.toSet()
        prefs.edit().putStringSet(KEY_TARGETS, raw).apply()
    }

    /**
     * Kills background processes not on the protected list, freeing RAM/CPU
     * headroom before launching a game/launcher.
     */
    fun killBackgroundApps(context: Context, keepPackage: String?) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return
        val runningApps = try {
            am.runningAppProcesses ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }

        val myPackage = context.packageName
        for (proc in runningApps) {
            val pkgs = proc.pkgList ?: continue
            for (pkg in pkgs) {
                if (pkg == myPackage || pkg == keepPackage) continue
                if (PROTECTED_PREFIXES.any { pkg.startsWith(it) }) continue
                try {
                    am.killBackgroundProcesses(pkg)
                } catch (e: SecurityException) {
                    // Ignore packages we don't have permission to kill.
                }
            }
        }
    }

    fun isDndAccessGranted(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    fun openDndAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun setDnd(context: Context, enable: Boolean) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return
        nm.setInterruptionFilter(
            if (enable) NotificationManager.INTERRUPTION_FILTER_PRIORITY
            else NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }
}
