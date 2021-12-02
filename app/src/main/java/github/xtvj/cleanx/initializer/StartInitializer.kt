package github.xtvj.cleanx.initializer

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.Initializer
import github.xtvj.cleanx.utils.initLog

class StartInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        //初始化操作

        //初始化log工具
        initLog()
        //"添加夜间模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}