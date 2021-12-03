package github.xtvj.cleanx.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.repository.AppRepository
import github.xtvj.cleanx.shell.RunnerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: AppRepository,
    private val appItemDao: AppItemDao
) : ViewModel() {

    val userList = Pager(
        PagingConfig(pageSize = 15)
    ) {
        //https://issuetracker.google.com/issues/175139766
        appItemDao.getUser()
    }.flow
        .cachedIn(viewModelScope)

    val systemList = Pager(
        PagingConfig(pageSize = 15)
    ) {
        appItemDao.getSystem()
    }.flow
        .cachedIn(viewModelScope)

    val disableList = Pager(
        PagingConfig(pageSize = 15)
    ) {
        appItemDao.getDisable()
    }.flow
        .cachedIn(viewModelScope)

    var userReload = MutableLiveData(false)
    var systemReload = MutableLiveData(false)
    var disableReload = MutableLiveData(false)


    suspend fun getUserApps() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getApps(RunnerUtils.GETUSER)
        }
    }

    suspend fun getSystemApps() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getApps(RunnerUtils.GETSYS)
        }
    }

    suspend fun getDisabledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getApps(RunnerUtils.GETDISABLED)
        }
    }

    suspend fun getAllApps() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getApps(RunnerUtils.GETAll)
        }
    }

    fun reFresh(type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                0 -> {
                    userReload.postValue(true)
                    userReload.postValue(withContext(Dispatchers.IO){
                        appItemDao.deleteAllUser()
                        !repository.getApps(RunnerUtils.GETUSER)
                    })
                }
                1 -> {
                    systemReload.postValue(true)
                    systemReload.postValue(withContext(Dispatchers.IO){
                        appItemDao.deleteAllSystem()
                        !repository.getApps(RunnerUtils.GETSYS)
                    })
                }
                else -> {
                    disableReload.postValue(true)
                    disableReload.postValue(withContext(Dispatchers.IO){
                        appItemDao.deleteAllDisable()
                        !repository.getApps(RunnerUtils.GETDISABLED)
                    })
                }
            }
        }

    }

}