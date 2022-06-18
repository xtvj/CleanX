package github.xtvj.cleanx.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.AppRepository
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.*
import github.xtvj.cleanx.worker.AppWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListViewModel @Inject constructor(
    private val appItemDao: AppItemDao,
    private val appRepository: AppRepository,
    private val state: SavedStateHandle,
    private val workManager: WorkManager
) : ViewModel() {

    companion object {
        private const val SORT_BY = "sortBy"
    }

    val sortByColumnFlow: MutableStateFlow<String> = MutableStateFlow(
        state.get(SORT_BY) ?: APPS_BY_NAME
    )

    //0:不过滤数据 1:过滤掉禁用/运行的 2:过滤掉启用/不运行的
    val filterEnable = MutableStateFlow<Int>(1)
    val filterRunning = MutableStateFlow<Int>(2)

    init {
        viewModelScope.launch {
            launch {
                sortByColumnFlow.collect { newSort ->
                    state.set(SORT_BY, newSort)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    val userList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            filterEnableOrRunning(appRepository.getUser(query, false).cachedIn(viewModelScope))
        } else {
            filterEnableOrRunning(appRepository.getUser(query, true).cachedIn(viewModelScope))
        }
    }

    @ExperimentalCoroutinesApi
    val systemList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            filterEnableOrRunning(appRepository.getSys(query, false).cachedIn(viewModelScope))
        } else {
            filterEnableOrRunning(appRepository.getSys(query, true).cachedIn(viewModelScope))
        }
    }

    @ExperimentalCoroutinesApi
    val disableList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            filterEnableOrRunning(appRepository.getDisable(query, false).cachedIn(viewModelScope))
        } else {
            filterEnableOrRunning(appRepository.getDisable(query, true).cachedIn(viewModelScope))
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
                when (code) {
                    PM_ENABLE -> {
                        for (item in list) {
                            appItemDao.updateEnable(item.id, true)
                        }
                    }
                    PM_DISABLE -> {
                        for (item in list) {
                            appItemDao.updateEnable(item.id, false)
                        }
                    }
                    FORCE_STOP -> {
                        for (item in list) {
                            appItemDao.updateRunning(item.id, false)
                        }
                    }
                }
            }
        }

    }

    fun getAppsByCode(code: String): LiveData<WorkInfo> {
        val request = OneTimeWorkRequestBuilder<AppWorker>()
            .setInputData(workDataOf(AppWorker.KEY_CODE to code))
            .build()
        workManager.enqueue(request)
        return workManager.getWorkInfoByIdLiveData(request.id)
    }

    private fun filterEnableOrRunning(list: Flow<PagingData<AppItem>>): Flow<PagingData<AppItem>> {
        return list
            .combine(filterEnable) { pagingData, filters ->
                pagingData.filter {
                    when(filters){
                        1 ->{
                            !it.isEnable
                        }
                        2 ->{
                            it.isEnable
                        }
                        else -> {
                            true
                        }
                    }
                }
            }.combine(filterRunning) { pagingData, filters ->
                pagingData.filter {
                    when(filters){
                        1 ->{
                            it.isRunning
                        }
                        2 ->{
                            !it.isRunning
                        }
                        else -> {
                            true
                        }
                    }
                }
            }
    }
}