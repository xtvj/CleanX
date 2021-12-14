package github.xtvj.cleanx.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
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

        //https://developer.android.com/topic/libraries/architecture/workmanager/advanced/custom-configuration#custom
        // provide custom configuration
        val myConfig = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

        // initialize WorkManager
        WorkManager.initialize(context, myConfig)


    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}