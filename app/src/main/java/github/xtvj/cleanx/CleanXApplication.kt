package github.xtvj.cleanx

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import github.xtvj.cleanx.utils.CoilHolder

@HiltAndroidApp
class CleanXApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        CoilHolder.init(this)
        
    }
}