package github.xtvj.cleanx.viewmodel

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.repository.AppRepository
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.shell.RunnerUtils
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(private val pm: PackageManager,private val repository: AppRepository) : ViewModel() {


    var listUser: MutableLiveData<List<AppItem>> = MutableLiveData<List<AppItem>>()
    var listSystem: MutableLiveData<List<AppItem>> = MutableLiveData<List<AppItem>>()
    var listDisable: MutableLiveData<List<AppItem>> = MutableLiveData<List<AppItem>>()

    fun getUserApps() {
        viewModelScope.launch(Dispatchers.IO) {
            getApps(RunnerUtils.GETUSER)
            listUser.postValue(repository.getUser())
        }
    }

    fun getSystemApps() {
        viewModelScope.launch(Dispatchers.IO) {
            getApps(RunnerUtils.GETSYS)
            listSystem.postValue(repository.getSystem())
        }
    }

    fun getDisabledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            getApps(RunnerUtils.GETDISABLED)
            listDisable.postValue(repository.getDisable())
        }

    }

    private suspend fun getApps(code: String) {
        //获取应用列表
        val result = Runner.runCommand(code)
        if (result.isSuccessful) {
           val temp =  result.getOutputAsList(0).map { s ->
                s.substring(8)
            }.sorted()
            for (i in temp) {
                try {
                    val appInfo = pm.getPackageInfo(i, PackageManager.GET_META_DATA)
                    val name = appInfo.applicationInfo.loadLabel(pm).toString()
                    val version = appInfo.versionName
                    val isSystem =
                        (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isEnable = appInfo.applicationInfo.enabled
                    val firstInstallTime = appInfo.firstInstallTime
                    val lastUpdateTime = appInfo.lastUpdateTime
                    val dataDir = appInfo.applicationInfo.dataDir
                    val sourceDir = appInfo.applicationInfo.sourceDir
                    val deviceProtectedDataDir = appInfo.applicationInfo.deviceProtectedDataDir
                    val publicSourceDir = appInfo.applicationInfo.publicSourceDir

                    val item = AppItem(
                        i,
                        name,
                        version,
                        isSystem,
                        isEnable,
                        firstInstallTime,
                        lastUpdateTime,
                        dataDir,
                        sourceDir,
                        deviceProtectedDataDir,
                        publicSourceDir
                    )
                    repository.insertAll(item)
                } catch (e: PackageManager.NameNotFoundException) {
                    log(e.toString())
                }
            }
        } else {
            log(result.toString())
        }
    }
}