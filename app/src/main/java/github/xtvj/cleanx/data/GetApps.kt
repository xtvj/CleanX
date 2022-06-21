package github.xtvj.cleanx.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.log

object GetApps {

    private const val FLAG_STOPPED = 1 shl 21

    fun getAppsByCode(pm: PackageManager, code: String): List<AppItem> {
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
                    val item = getItem(pm, i)
                    if (item != null) {
                        list.add(item)
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

    fun getItem(pm: PackageManager, appId: String): AppItem? {
        try {
            val appInfo = pm.getPackageInfo(appId, PackageManager.GET_META_DATA)
            val name = appInfo.applicationInfo.loadLabel(pm).toString()
            val version = appInfo.versionName ?: "null"
            val isSystem =
                (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isEnable = appInfo.applicationInfo.enabled
            val firstInstallTime = appInfo.firstInstallTime
            val lastUpdateTime = appInfo.lastUpdateTime
            val dataDir = appInfo.applicationInfo.dataDir
            val sourceDir = appInfo.applicationInfo.sourceDir
//                        val deviceProtectedDataDir =
//                            appInfo.applicationInfo.deviceProtectedDataDir
//                        val publicSourceDir = appInfo.applicationInfo.publicSourceDir
            val icon = appInfo.applicationInfo.icon
            val isRunning = (appInfo.applicationInfo.flags and FLAG_STOPPED) == 0
            val versionCode = appInfo.longVersionCode

            return AppItem(
                appId,
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
                isRunning,
                versionCode
            )
        } catch (e: Exception) {
            //改为Exception，是因为version 可能为空
            log("${e.message} -------- package: $appId")
            return null
        }
    }


}