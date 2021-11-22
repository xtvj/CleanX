package github.xtvj.cleanx.viewmodel

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.adapter.AppItem
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(private val pm: PackageManager) : ViewModel() {

    var list: MutableLiveData<List<AppItem>> = MutableLiveData<List<AppItem>>()

    fun upData(s : List<String>){
        viewModelScope.launch(Dispatchers.IO) {
            val items = ArrayList<AppItem>()
            for (i in s) {
                try {
                    val appInfo = pm.getPackageInfo(i,PackageManager.GET_META_DATA)
                    val name = appInfo.applicationInfo.loadLabel(pm).toString()
                    val version = appInfo.versionName
                    val isSystem  = (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val item = AppItem(i,name,version,isSystem)
                    items.add(item)
                } catch (e : PackageManager.NameNotFoundException) {
                    log(e.toString())
                }

            }
            list.postValue(items)
        }

    }
}