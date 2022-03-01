package github.xtvj.cleanx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppRepository
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ListViewModel @Inject constructor(
    private val appItemDao: AppItemDao,
    private val appRepository: AppRepository
//    private val workManager: WorkManager
) : ViewModel() {

//    var sortDirection = MutableLiveData(true)

    var sortByColumnFlow = MutableStateFlow(APPS_BY_NAME)

    @ExperimentalCoroutinesApi
    val userList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            appRepository.getUser(query,false).cachedIn(viewModelScope)
        } else {
            appRepository.getUser(query,true).cachedIn(viewModelScope)
        }
    }

    @ExperimentalCoroutinesApi
    val systemList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            appRepository.getSys(query,false).cachedIn(viewModelScope)
        } else {
            appRepository.getSys(query,true).cachedIn(viewModelScope)
        }
    }

    @ExperimentalCoroutinesApi
    val disableList = sortByColumnFlow.flatMapLatest { query ->
        if (query == APPS_BY_LAST_UPDATE_TIME) {
            appRepository.getDisable(query,false).cachedIn(viewModelScope)
        } else {
            appRepository.getDisable(query,true).cachedIn(viewModelScope)
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

//    fun getAppsByCode(code: String): LiveData<WorkInfo> {
//        val request = OneTimeWorkRequestBuilder<AppWorker>()
//            .setInputData(workDataOf(AppWorker.KEY_CODE to code))
//            .build()
//        workManager.enqueue(request)
//        return workManager.getWorkInfoByIdLiveData(request.id)
//    }

}