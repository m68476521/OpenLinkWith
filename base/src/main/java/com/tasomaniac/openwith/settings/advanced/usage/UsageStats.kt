package com.tasomaniac.openwith.settings.advanced.usage

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.getSystemService
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

object UsageStats {

    @JvmStatic
    fun observeAccessGiven(context: Context): Completable =
        observe(context)
            .filter { accessGiven -> accessGiven }
            .firstElement()
            .timeout(1, TimeUnit.MINUTES)
            .onErrorReturnItem(false)
            .ignoreElement()

    private fun observe(context: Context): Observable<Boolean> {
        return Observable.interval(1, TimeUnit.SECONDS)
            .map { isEnabled(context) }
    }

    @JvmStatic
    fun isEnabled(context: Context): Boolean {
        val appOps = context.getSystemService<AppOpsManager>() ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

@Suppress("TooGenericExceptionCaught")
@SuppressLint("InlinedApi")
fun Context.maybeStartUsageAccessSettings() = try {
    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    true
} catch (e: Exception) {
    Timber.e(e, "Usage Access Open")
    false
}
