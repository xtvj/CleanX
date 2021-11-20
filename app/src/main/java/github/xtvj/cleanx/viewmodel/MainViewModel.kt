package github.xtvj.cleanx.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.utils.AppUtils
import github.xtvj.cleanx.utils.ShellUtils
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var userapps: MutableLiveData<List<String>> = MutableLiveData<List<String>>()
    var systemapps: MutableLiveData<List<String>> = MutableLiveData<List<String>>()
    var disabledapps: MutableLiveData<List<String>> = MutableLiveData<List<String>>()
    val root: MutableLiveData<Boolean> = MutableLiveData()

    fun isAppRoot(): Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            root.postValue(AppUtils.isAppRoot)
        }
        return root.value == true
    }

    fun getUserApps() {
        viewModelScope.launch(Dispatchers.IO) {
            getApps("pm list packages -3", userapps)
        }
    }

    fun getSystemApps() {
        viewModelScope.launch(Dispatchers.IO) {
            getApps("pm list packages -s", systemapps)
        }
    }

    fun getDisabledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            getApps("pm list packages -d", disabledapps)
        }

    }


    private fun getApps(code: String, apps: MutableLiveData<List<String>>) {
        //获取应用列表
        val result: ShellUtils.CommandResult = ShellUtils.execCmd(code, true)
        if (result.result == 0) {
            //以"package:"分割字符，再去除空格，再丢掉空元素，最后根据包名默认排序
            apps.postValue(result.successMsg.split("package:").map { s ->
                s.filter { !it.isWhitespace() }
            }.dropWhile { it.isEmpty() }.sorted())
        } else {
            log(result.toString())
            apps.postValue(emptyList())
        }
    }

}