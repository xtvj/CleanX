package github.xtvj.cleanx.utils

import github.xtvj.cleanx.BuildConfig
import timber.log.Timber

fun log(message : String){
    if (BuildConfig.DEBUG){
        Timber.d(message)
    }
}

fun log(message : String, vararg args: Any?){
    if (BuildConfig.DEBUG){
        Timber.d(message,args)
    }
}

