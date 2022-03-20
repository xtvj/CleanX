package github.xtvj.cleanx.utils

import github.xtvj.cleanx.BuildConfig
import timber.log.Timber

//初始化log工具
fun initLog() {
    Timber.plant(Timber.DebugTree())
}

fun log(message: String) {
    if (BuildConfig.DEBUG) {
        Timber.d(message)
    }
}

fun log(message: String, vararg args: Any?) {
    if (BuildConfig.DEBUG) {
        Timber.d(message, args)
    }
}

