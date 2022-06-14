package github.xtvj.cleanx.data

import android.content.pm.PackageManager
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val appItemDao: AppItemDao,
    private val db: AppDatabase,
    private val pm: PackageManager
) {


    fun getUser(query: String, sort: Boolean): Flow<PagingData<AppItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = true
            ),
//            remoteMediator = AppRemoteMediator(
//                appItemDao,
//                db,
//                pm,
//                GET_USER
//            ),
            pagingSourceFactory = {
                appItemDao.getUser(query, sort)
            }
        ).flow
    }

    fun getSys(query: String, sort: Boolean): Flow<PagingData<AppItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = true
            ),
//            remoteMediator = AppRemoteMediator(
//                appItemDao,
//                db,
//                pm,
//                GET_SYS
//            ),
            pagingSourceFactory = {
                appItemDao.getSystem(query, sort)
            }
        ).flow
    }

    fun getDisable(query: String, sort: Boolean): Flow<PagingData<AppItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = true
            ),
//            remoteMediator = AppRemoteMediator(
//                appItemDao,
//                db,
//                pm,
//                GET_DISABLED
//            ),
            pagingSourceFactory = {
                appItemDao.getDisable(query, sort)
            }
        ).flow
    }


    companion object {
        private const val PAGE_SIZE = 20
    }
}