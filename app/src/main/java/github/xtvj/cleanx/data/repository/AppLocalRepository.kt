package github.xtvj.cleanx.data.repository

import androidx.paging.PagingSource
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import javax.inject.Inject

class AppLocalRepository @Inject constructor(private var itemDao: AppItemDao) {

    fun getAll() : PagingSource<Int,AppItem> {
        return itemDao.getAll()
    }

    fun getUser() : PagingSource<Int,AppItem>{
        return itemDao.getUser()
    }

    fun getSystem() : PagingSource<Int,AppItem> {
        return itemDao.getSystem()
    }
    fun getDisable() : PagingSource<Int,AppItem>{
        return itemDao.getDisable()
    }

    suspend fun insertAll(appItem: AppItem){
        itemDao.insertAll(appItem)
    }

    suspend fun deleteAll(){
        itemDao.deleteAll()
    }
}