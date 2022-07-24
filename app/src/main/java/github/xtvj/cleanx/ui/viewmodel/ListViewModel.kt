package github.xtvj.cleanx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.data.*
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListViewModel @Inject constructor(
    private val appItemDao: AppItemDao,
    private val appRepository: AppRepository,
//    private val state: SavedStateHandle,
    private val workManager: WorkManager,
    private val dataStoreManager:DataStoreManager
) : ViewModel() {

    //大部分系统应用的安装时间是同一个值，按时间排序的时候就可能出现第二次进入画面时禁用的被排在后面。待查。
    private val sortByColumnFlow: MutableStateFlow<String> = MutableStateFlow(APPS_BY_NAME)

    //0:不过滤数据 1:过滤掉禁用/运行的 2:过滤掉启用/不运行的
    private val filterEnable = MutableStateFlow<Int>(0)
    private val filterRunning = MutableStateFlow<Int>(0)
    private val asc = MutableStateFlow<Boolean>(true)

    //些处loading为三个页面共用了，不合适，以后再改。
//    val loading = MutableStateFlow<Boolean>(true)

    init {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                dataStoreManager.userPreferencesFlow.collectLatest {
                    when (it.sortOrder) {
                        SortOrder.BY_ID -> {
                            sortByColumnFlow.update { APPS_BY_ID }
                        }
                        SortOrder.BY_NAME -> {
                            sortByColumnFlow.update { APPS_BY_NAME }
                        }
                        SortOrder.BY_UPDATE_TIME -> {
                            sortByColumnFlow.update { APPS_BY_LAST_UPDATE_TIME }
                        }
                    }
                    filterEnable.value = it.enable
                    filterRunning.value = it.running
                    asc.value = it.asc
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    val userList = sortByColumnFlow.flatMapLatest { query ->
        asc.flatMapLatest {
            filterEnableOrRunning(appRepository.getUser(query, it).cachedIn(viewModelScope))
        }
    }

    @ExperimentalCoroutinesApi
    val systemList = sortByColumnFlow.flatMapLatest { query ->
        asc.flatMapLatest {
            filterEnableOrRunning(appRepository.getSys(query, it).cachedIn(viewModelScope))
        }
    }

    @ExperimentalCoroutinesApi
    val disableList = sortByColumnFlow.flatMapLatest { query ->
        asc.flatMapLatest {
            filterEnableOrRunning(appRepository.getDisable(query, it).cachedIn(viewModelScope))
        }
    }

    fun setApps(code: String, list: List<AppItem>,loading: MutableStateFlow<Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            loading.value = true
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
            loading.value = false
        }

    }

//    fun getAppsByCode(code: String): LiveData<WorkInfo> {
//        val request = OneTimeWorkRequestBuilder<AppWorker>()
//            .setInputData(workDataOf(AppWorker.KEY_CODE to code))
//            .build()
//        workManager.enqueue(request)
//        return workManager.getWorkInfoByIdLiveData(request.id)
//    }

    private fun filterEnableOrRunning(list: Flow<PagingData<AppItem>>): Flow<PagingData<AppItem>> {
        return combine(list, filterEnable, filterRunning) { temp, enable, running ->
            temp.filter {
                it.id.isNotEmpty() && when (enable) {
                    1 -> {
                        !it.isEnable
                    }
                    2 -> {
                        it.isEnable
                    }
                    else -> {
                        true
                    }
                } && when (running) {
                    1 -> {
                        it.isRunning
                    }
                    2 -> {
                        !it.isRunning
                    }
                    else -> {
                        true
                    }
                }
            }
        }.distinctUntilChanged()
    }
}