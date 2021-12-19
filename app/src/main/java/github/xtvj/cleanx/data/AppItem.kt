package github.xtvj.cleanx.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "appItem"
)
@Parcelize
data class AppItem(
    @PrimaryKey var id: String,
    @ColumnInfo(name = "name") var name: String,
    var version: String,
    var isSystem: Boolean,
    var isEnable: Boolean,
    var firstInstallTime: String,
    var lastUpdateTime: String,
    var dataDir: String,
    var sourceDir: String,
    var deviceProtectedDataDir: String?,
    var publicSourceDir: String,
    var icon:Int,
    var isRunning: Boolean

    //可以使用@Ignore忽略不想在数据库中储存的字段
    //@ColumnInfo为字段在数据库中重命名

) : Parcelable {


}

