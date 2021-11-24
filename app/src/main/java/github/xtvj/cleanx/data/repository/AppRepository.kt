package github.xtvj.cleanx.data.repository

import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import javax.inject.Inject

class AppRepository @Inject constructor(private var itemDao: AppItemDao) {

    suspend fun getAll() : List<AppItem> {
        return itemDao.getAll()
    }

    suspend fun getUser() : List<AppItem>{
        return itemDao.getUser()
    }

    suspend fun getSystem() : List<AppItem>{
        return itemDao.getSystem()
    }
    suspend fun getDisable() : List<AppItem>{
        return itemDao.getDisable()
    }

    suspend fun insertAll(appItem: AppItem){
        itemDao.insertAll(appItem)
    }
}