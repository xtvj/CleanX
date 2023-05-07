package github.xtvj.cleanx.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import github.xtvj.cleanx.data.AppDatabase
import github.xtvj.cleanx.data.GetApps
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

class InstallReceiver @Inject constructor(
    private val appDatabase: AppDatabase,
    private val packageManager: PackageManager
) :
    BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_REMOVED || intent?.action == Intent.ACTION_PACKAGE_FULLY_REMOVED) {
            //卸载应用监听
            val packageName = intent.dataString?.substring(8)?.lowercase(Locale.getDefault())
            log("packageName remove: $packageName")
            if (!packageName.isNullOrBlank()) {
                runBlocking(Dispatchers.IO) {
                    appDatabase.appItemDao().deleteByID(packageName)
                }
            }
        } else if (intent?.action == Intent.ACTION_PACKAGE_ADDED) {
            //安装应用监听
            val packageName = intent.dataString?.substring(8)?.lowercase(Locale.getDefault())
            log("packageName install: $packageName")
            runBlocking(Dispatchers.IO) {
                val item = packageName?.let { GetApps.getItem(packageManager, it) }
                item?.let { appDatabase.appItemDao().insertAll(it) }
            }

        }
    }

}