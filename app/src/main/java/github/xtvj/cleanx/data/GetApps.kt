package github.xtvj.cleanx.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import github.xtvj.cleanx.data.entity.AppItem
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.DateUtil
import github.xtvj.cleanx.utils.log

object GetApps{

    val FLAG_STOPPED = 1 shl 21

    fun getAppsByCode(pm: PackageManager,code: String): List<AppItem> {
        if (code.isNotEmpty()) {
            log("get app list by RemoteMediator $code")
            //获取应用列表
            val result = Runner.runCommand(Runner.userInstance(), code)
            if (result.isSuccessful) {
                val temp = result.getOutputAsList(0).map { s ->
                    s.substring(8)
                }.sorted()
                val list = mutableListOf<AppItem>()
                for (i in temp) {
                    try {
                        val appInfo = pm.getPackageInfo(i, PackageManager.GET_META_DATA)
                        val name = appInfo.applicationInfo.loadLabel(pm).toString()
                        val version = appInfo.versionName
                        val isSystem =
                            (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val isEnable = appInfo.applicationInfo.enabled
                        val firstInstallTime = DateUtil.format(appInfo.firstInstallTime)
                        val lastUpdateTime = DateUtil.format(appInfo.lastUpdateTime)
                        val dataDir = appInfo.applicationInfo.dataDir
                        val sourceDir = appInfo.applicationInfo.sourceDir
//                        val deviceProtectedDataDir =
//                            appInfo.applicationInfo.deviceProtectedDataDir
//                        val publicSourceDir = appInfo.applicationInfo.publicSourceDir
                        val icon = appInfo.applicationInfo.icon
                        val isRunning = (appInfo.applicationInfo.flags and FLAG_STOPPED) == 0

                        val item = AppItem(
                            i,
                            name,
                            version,
                            isSystem,
                            isEnable,
                            firstInstallTime,
                            lastUpdateTime,
                            dataDir,
                            sourceDir,
//                          deviceProtectedDataDir,
//                          publicSourceDir,
                            icon,
                            isRunning
                        )
                        list.add(item)
                    } catch (e: PackageManager.NameNotFoundException) {
                        log(e.toString())
                    }
                }
                return list
            } else {
                log(result.toString())
                return emptyList()
            }
        }
        return emptyList()
    }
}