package github.xtvj.cleanx.viewmodel

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.xtvj.cleanx.adapter.SimpleItem
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ListViewModel : ViewModel() {

    var list: MutableLiveData<List<SimpleItem>> = MutableLiveData<List<SimpleItem>>()

    fun upData(s : List<String>,pm : PackageManager){
        viewModelScope.launch(Dispatchers.IO) {
            val items = ArrayList<SimpleItem>()
            for (i in s) {
                try {
                    val item = SimpleItem()
                    val appInfo = pm.getPackageInfo(i,PackageManager.GET_META_DATA)
                    val name = appInfo.applicationInfo.loadLabel(pm).toString()
                    val icon = appInfo.applicationInfo.loadIcon(pm)
                    val version = appInfo.versionName
                    val isSystem  = (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    item.withID(i,name,version,icon,isSystem)
                    items.add(item)
                } catch (e : PackageManager.NameNotFoundException) {
                    log(e.toString())
                }

            }
            list.postValue(items)
        }

    }
}