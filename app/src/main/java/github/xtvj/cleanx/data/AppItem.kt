package github.xtvj.cleanx.data

data class AppItem(
    var id: String,
    var name: String,
    var version: String,
    var isSystem: Boolean,
    var isEnable: Boolean,
    var firstInstallTime: Long,
    var lastUpdateTime: Long,
    var dataDir: String,
    var sourceDir: String,
    var deviceProtectedDataDir: String?,
    var publicSourceDir: String
)
