package github.xtvj.cleanx.data.repository

import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppLocalRepository @Inject constructor(private var itemDao: AppItemDao) {

    fun getAll() : Flow<List<AppItem>> {
        return itemDao.getAll()
    }

    fun getUser() : Flow<List<AppItem>>{
        return itemDao.getUser()
    }

    fun getSystem() : Flow<List<AppItem>>{
        return itemDao.getSystem()
    }
    fun getDisable() : Flow<List<AppItem>>{
        return itemDao.getDisable()
    }

    suspend fun insertAll(appItem: AppItem){
        itemDao.insertAll(appItem)
    }

    suspend fun deleteAll(){
        itemDao.deleteAll()
    }
}