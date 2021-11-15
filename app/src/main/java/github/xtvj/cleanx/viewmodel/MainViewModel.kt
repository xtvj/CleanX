package github.xtvj.cleanx.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.xtvj.cleanx.dto.AppInfo
import github.xtvj.cleanx.utils.AppUtils
import github.xtvj.cleanx.utils.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val runningAppLiveData = MutableLiveData<List<AppInfo>>()
    val runningLiveData: LiveData<List<AppInfo>> get() = runningAppLiveData
    var userapps:MutableLiveData<List<String>> = MutableLiveData<List<String>>()
    var systemapps:MutableLiveData<List<String>> = MutableLiveData<List<String>>()
    var disabledapps:MutableLiveData<List<String>> = MutableLiveData<List<String>>()
    val root: MutableLiveData<Boolean> = MutableLiveData()
    private var TAG  = "MainViewModel"

    fun isAppRoot() : Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            root.postValue(AppUtils.isAppRoot())
        }
        return root.value == true
    }

    fun getUserApps(){
        viewModelScope.launch(Dispatchers.IO) {
            getAapps("pm list packages -3",userapps)
        }
    }

    fun getSystemApps(){
        viewModelScope.launch(Dispatchers.IO) {
            getAapps("pm list packages -s",systemapps)
        }
    }

    fun getDisabledApps(){
        viewModelScope.launch(Dispatchers.IO) {
            getAapps("pm list packages -d",disabledapps)
        }

    }



    private fun getAapps(code : String , apps : MutableLiveData<List<String>>){
        //获取应用列表
        val result : ShellUtils.CommandResult = ShellUtils.execCmd(code, true)
        if (result.result == 0){
            apps.postValue(result.successMsg.split("package:").dropWhile { it.isEmpty() })
            Log.d(TAG, result.toString())
        }else{
            apps.postValue(emptyList())
        }
    }

}