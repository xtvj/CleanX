package github.xtvj.cleanx.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import github.xtvj.cleanx.data.AppDatabase
import github.xtvj.cleanx.data.AppWorker
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.utils.ALL_UUID
import github.xtvj.cleanx.utils.GET_All
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
            val request = OneTimeWorkRequestBuilder<AppWorker>()
                .setInputData(workDataOf(AppWorker.KEY_CODE to GET_All))
                .build()
            ALL_UUID = request.id
            WorkManager.getInstance(context).enqueue(request)
        }

        //初始化log工具
        initLog()


    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}