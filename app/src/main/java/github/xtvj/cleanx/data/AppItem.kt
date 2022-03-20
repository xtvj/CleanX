package github.xtvj.cleanx.data

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import github.xtvj.cleanx.R
import github.xtvj.cleanx.utils.DateUtil
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
    var firstInstallTime: Long,
    var lastUpdateTime: Long,
    var dataDir: String,
    var sourceDir: String,
    var icon: Int,
    var isRunning: Boolean,

    @ColumnInfo(defaultValue = "1")
    var versionCode: Long

    //可以使用@Ignore忽略不想在数据库中储存的字段
    //@ColumnInfo为字段在数据库中重命名

) : Parcelable {


    fun getFormatUpdateTime(): String {
        return DateUtil.format(lastUpdateTime)
    }

    fun getIconUri(): Any {
        return if (icon != 0) {
            Uri.parse("android.resource://$id/$icon")
        } else {
            R.drawable.ic_default_round
        }
    }

}

