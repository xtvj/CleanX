package github.xtvj.cleanx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.AppWorker
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


@HiltViewModel
class ListViewModel @Inject constructor(
    private val appItemDao: AppItemDao,
    private val workManager: WorkManager
) : ViewModel() {

//    var sortDirection = MutableLiveData(true)

    var sortByColumnFlow = MutableStateFlow(APPS_BY_NAME)

    @ExperimentalCoroutinesApi
    val userList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            Pager(PagingConfig(pageSize = 12, prefetchDistance = 5)) {
                appItemDao.getUser(query, false)
            }.flow.cachedIn(viewModelScope)
        } else {
            Pager(PagingConfig(pageSize = 12, prefetchDistance = 5)) {
                appItemDao.getUser(query, true)
            }.flow.cachedIn(viewModelScope)
        }
    }

    @ExperimentalCoroutinesApi
    val systemList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            Pager(PagingConfig(pageSize = 12, prefetchDistance = 5)) {
                appItemDao.getSystem(query, false)
            }.flow.cachedIn(viewModelScope)
        } else {
            Pager(PagingConfig(pageSize = 12, prefetchDistance = 5)) {
                appItemDao.getSystem(query, true)
            }.flow.cachedIn(viewModelScope)
        }
    }

    @ExperimentalCoroutinesApi
    val disableList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            Pager(PagingConfig(pageSize = 10, prefetchDistance = 5)) {
                appItemDao.getDisable(query, false)
            }.flow.cachedIn(viewModelScope)
        } else {
            Pager(PagingConfig(pageSize = 10, prefetchDistance = 5)) {
                appItemDao.getDisable(query, true)
            }.flow.cachedIn(viewModelScope)
        }
    }

    suspend fun reFreshAppsByType(type: Int) =
        withContext(Dispatchers.IO) {
            when (type) {
                0 -> {
                    appItemDao.deleteAllUser()
                    getAppsByCode(GET_USER)
                }
                1 -> {
                    appItemDao.deleteAllSystem()
                    getAppsByCode(GET_SYS)
                }
                else -> {
                    appItemDao.deleteAllDisable()
                    getAppsByCode(GET_DISABLED)

                }
            }
        }


    fun setApps(code: String, list: List<AppItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            //可以添加弹窗提示正在执行中。由于执行时间过于短暂，暂时不添加
            val builder = StringBuilder()
            for (item in list) {
                builder.append(code + item.id + "\n")
            }
            val result = Runner.runCommand(Runner.rootInstance(), builder.toString())
            log(result.output)
            if (result.isSuccessful) {
                when(code){
                    PM_ENABLE ->{
                        for (item in list) {
                            appItemDao.updateEnable(item.id, true)
                        }
                    }
                    PM_DISABLE ->{
                        for (item in list) {
                            appItemDao.updateEnable(item.id, false)
                        }
                    }
                    FORCE_STOP ->{
                        for (item in list) {
                            appItemDao.updateRunning(item.id, false)
                        }
                    }
                }
            }
        }

    }

    fun getAppsByCode(code: String): UUID {
        val request = OneTimeWorkRequestBuilder<AppWorker>()
            .setInputData(workDataOf(AppWorker.KEY_CODE to code))
            .build()
        workManager.enqueue(request)
        return request.id
    }

}