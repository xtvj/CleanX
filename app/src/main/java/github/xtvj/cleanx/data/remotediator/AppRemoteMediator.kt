package github.xtvj.cleanx.data.remotediator

import android.content.pm.PackageManager
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import github.xtvj.cleanx.data.GetApps
import github.xtvj.cleanx.data.dao.AppItemDao
import github.xtvj.cleanx.data.db.AppDatabase
import github.xtvj.cleanx.data.entity.AppItem
import github.xtvj.cleanx.utils.GET_DISABLED
import github.xtvj.cleanx.utils.GET_SYS
import github.xtvj.cleanx.utils.GET_USER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@ExperimentalPagingApi
class AppRemoteMediator(
    private val appItemDao: AppItemDao,
    private val db: AppDatabase,
    private val pm: PackageManager,
    private val code: String
) :
    RemoteMediator<Int, AppItem>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AppItem>
    ): MediatorResult {

        try {
            if (loadType == LoadType.REFRESH || loadType == LoadType.PREPEND) {
                val list = withContext(Dispatchers.IO) {
                    GetApps.getAppsByCode(pm, code)
                }

                db.withTransaction {
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
                }
            }

            return MediatorResult.Success(true)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        }
    }

}