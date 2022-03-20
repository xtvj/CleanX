package github.xtvj.cleanx.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import github.xtvj.cleanx.data.AppItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class InstallWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val appItemDao: AppItemDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val id = inputData.getString(KEY_CODE)
            id?.let { appItemDao.deleteByID(it) }
            Result.success()
        }
    }

    companion object {
        const val KEY_CODE = "install_or_remove_app_name"
    }
}