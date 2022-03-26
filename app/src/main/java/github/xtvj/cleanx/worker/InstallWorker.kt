package github.xtvj.cleanx.worker

import android.content.Context
import android.content.pm.PackageManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.GetApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class InstallWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val appItemDao: AppItemDao,
    val packageManager: PackageManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val id = inputData.getString(KEY_ID)
            val isRemove = inputData.getBoolean(KEY_CODE,false)
            if (isRemove){
                id?.let { appItemDao.deleteByID(it) }
            }else{
                val item = id?.let { GetApps.getItem(packageManager, it) }
                item?.let { appItemDao.insertAll(it) }
            }
            Result.success()
        }
    }

    companion object {
        const val KEY_CODE = "install_or_remove_app_name"
        const val KEY_ID = "app_id"
    }
}