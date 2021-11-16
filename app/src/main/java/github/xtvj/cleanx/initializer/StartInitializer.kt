package github.xtvj.cleanx.initializer

import android.content.Context
import androidx.startup.Initializer
import timber.log.Timber

class StartInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        //初始化操作

        //初始化log工具
        Timber.plant(Timber.DebugTree())

        //TODO("添加夜间模式切换操作")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}