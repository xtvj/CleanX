package github.xtvj.cleanx.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import github.xtvj.cleanx.data.AppDatabase
import github.xtvj.cleanx.data.GetApps
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class InstallReceiver @Inject constructor(
    private val appDatabase: AppDatabase,
    private val packageManager: PackageManager
) : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        val packageName = intent?.data?.schemeSpecificPart ?: return
        val action = intent.action ?: return
        if (action != Intent.ACTION_PACKAGE_REMOVED &&
            action != Intent.ACTION_PACKAGE_FULLY_REMOVED &&
            action != Intent.ACTION_PACKAGE_ADDED
        ) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                when (action) {
                    Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                        if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                            appDatabase.appItemDao().deleteByID(packageName)
                        }
                    }
                    Intent.ACTION_PACKAGE_ADDED -> {
                        GetApps.getItem(packageManager, packageName)?.let {
                            appDatabase.appItemDao().insertAll(it)
                        }
                    }
                }
            } catch (exception: Exception) {
                log("Package update failed for $packageName: ${exception.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
