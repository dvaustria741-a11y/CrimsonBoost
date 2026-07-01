package com.zen.crimsonboost

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuHelper {
    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const val REQUEST_CODE = 9001

    fun isShizukuInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0); true
    } catch (e: PackageManager.NameNotFoundException) { false }

    fun isAvailable(): Boolean = try { Shizuku.pingBinder() } catch (e: Throwable) { false }

    fun hasPermission(): Boolean = try {
        isAvailable() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (e: Throwable) { false }

    fun requestPermission(onResult: (Boolean) -> Unit) {
        if (!isAvailable()) { onResult(false); return }
        val listener = object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                if (requestCode == REQUEST_CODE) {
                    onResult(grantResult == PackageManager.PERMISSION_GRANTED)
                    Shizuku.removeRequestPermissionResultListener(this)
                }
            }
        }
        Shizuku.addRequestPermissionResultListener(listener)
        try { Shizuku.requestPermission(REQUEST_CODE) }
        catch (e: Throwable) { Shizuku.removeRequestPermissionResultListener(listener); onResult(false) }
    }

    fun runShellCommand(command: String): Boolean {
        if (!hasPermission()) return false
        return try {
            val p = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            p.waitFor(); p.exitValue() == 0
        } catch (e: Throwable) { false }
    }
}
