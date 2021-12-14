package github.xtvj.cleanx.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.DateUtil
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AppWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {

            try {

                val appItemDao = AppDatabase.getInstance(context).appItemDao()
                val pm = context.packageManager

                val code = inputData.getString(KEY_CODE)
                if (code != null) {
                    log("get app list by worker $code")
                    //获取应用列表
                    val result = Runner.runCommand(Runner.userInstance(), code)
                    if (result.isSuccessful) {
                        val temp = result.getOutputAsList(0).map { s ->
                            s.substring(8)
                        }.sorted()
                        for (i in temp) {
                            try {
                                val appInfo = pm.getPackageInfo(i, PackageManager.GET_META_DATA)
                                val name = appInfo.applicationInfo.loadLabel(pm).toString()
                                val version = appInfo.versionName
                                val isSystem =
                                    (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                val isEnable = appInfo.applicationInfo.enabled
                                val firstInstallTime = DateUtil.format(appInfo.firstInstallTime)
                                val lastUpdateTime = DateUtil.format(appInfo.lastUpdateTime)
                                val dataDir = appInfo.applicationInfo.dataDir
                                val sourceDir = appInfo.applicationInfo.sourceDir
                                val deviceProtectedDataDir =
                                    appInfo.applicationInfo.deviceProtectedDataDir
                                val publicSourceDir = appInfo.applicationInfo.publicSourceDir
                                val icon = appInfo.applicationInfo.icon

                                val item = AppItem(
                                    i,
                                    name,
                                    version,
                                    isSystem,
                                    isEnable,
                                    firstInstallTime,
                                    lastUpdateTime,
                                    dataDir,
                                    sourceDir,
                                    deviceProtectedDataDir,
                                    publicSourceDir,
                                    icon
                                )
                                appItemDao.insertAll(item)
                            } catch (e: PackageManager.NameNotFoundException) {
                                log(e.toString())
                            }
                        }
                    } else {
                        log(result.toString())
                        Result.failure()
                    }
                    Result.success()
                } else {
                    Result.failure()
                }
            } catch (e: Exception) {
                log(e.message.toString())
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_CODE = "get_apps_by_code"
    }

}