package github.xtvj.cleanx.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import github.xtvj.cleanx.data.dao.AppItemDao
import github.xtvj.cleanx.data.entity.AppItem
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.DateUtil
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
使用AppRemoteMediator请求数据
弃用WorkManager 保留此类待以后修改
 */
@HiltWorker
class AppWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val appItemDao: AppItemDao,
    private val pm: PackageManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val FLAG_STOPPED = 1 shl 21
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
//                                val deviceProtectedDataDir =
//                                    appInfo.applicationInfo.deviceProtectedDataDir
//                                val publicSourceDir = appInfo.applicationInfo.publicSourceDir
                                val icon = appInfo.applicationInfo.icon
                                val isRunning = (appInfo.applicationInfo.flags and FLAG_STOPPED) == 0

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
//                                    deviceProtectedDataDir,
//                                    publicSourceDir,
                                    icon,
                                    isRunning
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