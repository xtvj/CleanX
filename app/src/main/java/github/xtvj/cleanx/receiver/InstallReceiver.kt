package github.xtvj.cleanx.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import github.xtvj.cleanx.utils.log
import github.xtvj.cleanx.worker.InstallWorker
import java.util.*
import javax.inject.Inject

class InstallReceiver @Inject constructor(private val workManager: WorkManager) :
    BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_REMOVED) {
            //卸载应用监听
            val packageName = intent.dataString?.substring(8)?.lowercase(Locale.getDefault())
            log("packageName remove: $packageName")
            if (!packageName.isNullOrBlank()) {
                val request = OneTimeWorkRequestBuilder<InstallWorker>()
                    .setInputData(workDataOf(InstallWorker.KEY_ID to packageName,InstallWorker.KEY_CODE to true))
                    .build()
                workManager.enqueue(request)
            }
        }
        else if (intent?.action == Intent.ACTION_PACKAGE_ADDED){
            //安装应用监听
            //刷新界面即可
            val packageName = intent.dataString?.substring(8)?.lowercase(Locale.getDefault())
            log("packageName install: $packageName")
            if (!packageName.isNullOrBlank()) {
                val request = OneTimeWorkRequestBuilder<InstallWorker>()
                    .setInputData(workDataOf(InstallWorker.KEY_ID to packageName,InstallWorker.KEY_CODE to false))
                    .build()
                workManager.enqueue(request)
            }
        }
    }

}