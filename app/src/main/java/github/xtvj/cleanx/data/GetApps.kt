package github.xtvj.cleanx.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.log
import java.io.IOException

object GetApps {

    private const val FLAG_STOPPED = 1 shl 21

    fun getAppsByCode(pm: PackageManager, code: String): List<AppItem> {
        if (code.isNotEmpty()) {
            log("get app list by RemoteMediator $code")
            //获取应用列表
            val result = Runner.runCommand(Runner.userInstance(), code)
            if (result.isSuccessful) {
                val temp = result.getOutputAsList(0)
                    .mapNotNull { line ->
                        line.trim().takeIf { it.startsWith("package:") }
                            ?.removePrefix("package:")
                            ?.takeIf { it.isNotBlank() }
                    }
                    .sorted()
                val list = mutableListOf<AppItem>()
                for (i in temp) {
                    val item = getItem(pm, i)
                    if (item != null) {
                        list.add(item)
                    }
                }
                return list
            } else {
                throw IOException("Package list command failed: $code")
            }
        }
        return emptyList()
    }

    fun getItem(pm: PackageManager, appId: String): AppItem? {
        try {
            val appInfo = pm.getPackageInfo(appId, PackageManager.GET_META_DATA)
            val applicationInfo = appInfo.applicationInfo ?: return null
            val name = applicationInfo.loadLabel(pm).toString()
            val version = appInfo.versionName ?: "null"
            val isSystem =
                (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isEnable = applicationInfo.enabled
            val firstInstallTime = appInfo.firstInstallTime
            val lastUpdateTime = appInfo.lastUpdateTime
            val dataDir = applicationInfo.dataDir
            val sourceDir = applicationInfo.sourceDir
            val icon = applicationInfo.icon
            val isRunning = (applicationInfo.flags and FLAG_STOPPED) == 0
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                appInfo.versionCode.toLong()
            }

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
