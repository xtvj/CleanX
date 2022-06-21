package github.xtvj.cleanx.worker

import android.content.Context
import android.content.pm.PackageManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.GetApps
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.GET_DISABLED
import github.xtvj.cleanx.utils.GET_SYS
import github.xtvj.cleanx.utils.GET_USER
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                val code = inputData.getString(KEY_CODE)
                if (code != null) {
                    log("get app list by worker $code")
                    //获取应用列表
                    val result = Runner.runCommand(Runner.userInstance(), code)
                    if (result.isSuccessful) {
                        val temp = result.getOutputAsList(0).map { s ->
                            s.substring(8)
                        }.sorted()
                        val list = mutableListOf<AppItem>()
                        for (i in temp) {
                            val item = GetApps.getItem(pm, i)
                            if (item != null) {
                                list.add(item)
                            }
                        }
                        when (code) {
                            GET_USER -> {
                                appItemDao.deleteAllUser()
                            }
                            GET_SYS -> {
                                appItemDao.deleteAllSystem()
                            }
                            GET_DISABLED -> {
                                appItemDao.deleteAllDisable()
                            }
                        }
                        appItemDao.insertMultipleItems(list)
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