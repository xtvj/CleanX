package github.xtvj.cleanx.initializer

import android.content.Context
import androidx.startup.Initializer
import github.xtvj.cleanx.data.AppDatabase
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.utils.ThemeHelper
import github.xtvj.cleanx.utils.initLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        //初始化操作

        CoroutineScope(Dispatchers.IO).launch{
            //启动主题
            val dataStoreManager = DataStoreManager(context)
            ThemeHelper.applyTheme(dataStoreManager.fetchInitialPreferences().darkModel)
            //清空数据库
            AppDatabase.getInstance(context).appItemDao().deleteAll()
        }

        //初始化log工具
        initLog()

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}