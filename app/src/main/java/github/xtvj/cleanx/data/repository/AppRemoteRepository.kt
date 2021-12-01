package github.xtvj.cleanx.data.repository

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.utils.log
import javax.inject.Inject

class AppRemoteRepository @Inject constructor(
    private val pm: PackageManager,
    private val itemDao: AppItemDao
) {


    /*
    val GETAll = "$CMD_PM list packages"
    val GETUSER = "$CMD_PM list packages -3"
    val GETSYS = "$CMD_PM list packages -s"
    val GETDISABLED = "$CMD_PM list packages -d"
     */
    suspend fun getApps(code: String) {
            log("get app list $code")
            //获取应用列表
            val result = Runner.runCommand(code)
            if (result.isSuccessful) {
                val temp = result.getOutputAsList(0).map { s ->
                    s.substring(8)
                }.sorted()
                for (i in temp) {
                    try {
                        val appInfo = pm.getPackageInfo(i, PackageManager.GET_META_DATA)
                        val name = appInfo.applicationInfo.loadLabel(pm).toString()
                        val version = appInfo.versionName
                        val isSystem =
                            (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val isEnable = appInfo.applicationInfo.enabled
                        val firstInstallTime = appInfo.firstInstallTime
                        val lastUpdateTime = appInfo.lastUpdateTime
                        val dataDir = appInfo.applicationInfo.dataDir
                        val sourceDir = appInfo.applicationInfo.sourceDir
                        val deviceProtectedDataDir = appInfo.applicationInfo.deviceProtectedDataDir
                        val publicSourceDir = appInfo.applicationInfo.publicSourceDir

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
                            deviceProtectedDataDir,
                            publicSourceDir
                        )
                        itemDao.insertAll(item)
                    } catch (e: PackageManager.NameNotFoundException) {
                        log(e.toString())
                    }
                }
            } else {
                log(result.toString())
            }
    }
}