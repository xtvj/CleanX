package github.xtvj.cleanx.data

import android.content.pm.PackageManager
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import github.xtvj.cleanx.data.GetApps
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.AppDatabase
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.utils.GET_DISABLED
import github.xtvj.cleanx.utils.GET_SYS
import github.xtvj.cleanx.utils.GET_USER
import github.xtvj.cleanx.utils.log
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

    override suspend fun initialize(): InitializeAction {
        // Require that remote REFRESH is launched on initial load and succeeds before launching
        //默认就是此值
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AppItem>
    ): MediatorResult {

        try {
            log("RemoteMediator loadType: ${loadType.name}")

            if (loadType == LoadType.REFRESH) {
                val list = withContext(Dispatchers.Default) {
                    GetApps.getAppsByCode(pm, code)
                }

                db.withTransaction {
                    log("RemoteMediator code: $code")
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
                return MediatorResult.Success(true)
            } else {
                return MediatorResult.Success(false)
            }

        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        }
    }

}