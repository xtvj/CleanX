package github.xtvj.cleanx.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "appItem"
)
data class AppItem(
    @PrimaryKey var id: String,
    @ColumnInfo(name = "name") var name: String,
    var version: String,
    var isSystem: Boolean,
    var isEnable: Boolean,
    var firstInstallTime: Long,
    var lastUpdateTime: Long,
    var dataDir: String,
    var sourceDir: String,
    var deviceProtectedDataDir: String?,
    var publicSourceDir: String

    //可以使用@Ignore忽略不想在数据库中储存的字段
    //@ColumnInfo为字段在数据库中重命名
)
