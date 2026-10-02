package github.xtvj.cleanx.data

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import github.xtvj.cleanx.R
import github.xtvj.cleanx.utils.DateUtil

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
    var icon: Int,
    var isRunning: Boolean,

    @ColumnInfo(defaultValue = "1")
    var versionCode: Long

    //可以使用@Ignore忽略不想在数据库中储存的字段
    //@ColumnInfo为字段在数据库中重命名

) : Parcelable {

    private constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readByte().toInt() != 0,
        parcel.readByte().toInt() != 0,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readByte().toInt() != 0,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(version)
        parcel.writeByte(if (isSystem) 1.toByte() else 0.toByte())
        parcel.writeByte(if (isEnable) 1.toByte() else 0.toByte())
        parcel.writeLong(firstInstallTime)
        parcel.writeLong(lastUpdateTime)
        parcel.writeString(dataDir)
        parcel.writeString(sourceDir)
        parcel.writeInt(icon)
        parcel.writeByte(if (isRunning) 1.toByte() else 0.toByte())
        parcel.writeLong(versionCode)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<AppItem> {
        override fun createFromParcel(parcel: Parcel) = AppItem(parcel)
        override fun newArray(size: Int): Array<AppItem?> = arrayOfNulls(size)
    }


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

