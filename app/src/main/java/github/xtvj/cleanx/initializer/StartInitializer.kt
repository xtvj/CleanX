package github.xtvj.cleanx.initializer

import android.content.Context
import androidx.startup.Initializer
import github.xtvj.cleanx.utils.initLog

class StartInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        //初始化操作

        //初始化log工具
        initLog()

        //todo初始化夜间模式
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}