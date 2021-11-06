package github.xtvj.cleanx.viewmodel

import android.app.ActivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.xtvj.cleanx.dto.AppInfo
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val runningAppLiveData = MutableLiveData<List<AppInfo>>()
    val runningLiveData: LiveData<List<AppInfo>> get() = runningAppLiveData
    lateinit var runningList: List<ActivityManager.RunningServiceInfo>
    var info:MutableLiveData<String> = MutableLiveData<String>()


    fun getRunningApp(){
        viewModelScope.launch {
//            val am = context.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
//            val rs = am.getRunningServices(50)
//            runningList = rs as List<ActivityManager.RunningServiceInfo>
            runningList.forEach {
                info.value = "$it/r/n"
    //            + "clientCount:" + it.clientCount
    //            + it.clientLabel + it.clientPackage + it.flags + it.foreground
    //            +it.process + it.service + it.started + it.uid
            }
        }


    }

}