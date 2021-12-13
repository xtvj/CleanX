package github.xtvj.cleanx.data

import android.os.Parcel
import android.os.Parcelable
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
    var firstInstallTime: String,
    var lastUpdateTime: String,
    var dataDir: String,
    var sourceDir: String,
    var deviceProtectedDataDir: String?,
    var publicSourceDir: String,
    var icon:Int

    //可以使用@Ignore忽略不想在数据库中储存的字段
    //@ColumnInfo为字段在数据库中重命名

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(version)
        parcel.writeByte(if (isSystem) 1 else 0)
        parcel.writeByte(if (isEnable) 1 else 0)
        parcel.writeString(firstInstallTime)
        parcel.writeString(lastUpdateTime)
        parcel.writeString(dataDir)
        parcel.writeString(sourceDir)
        parcel.writeString(deviceProtectedDataDir)
        parcel.writeString(publicSourceDir)
        parcel.writeInt(icon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppItem> {
        override fun createFromParcel(parcel: Parcel): AppItem {
            return AppItem(parcel)
        }

        override fun newArray(size: Int): Array<AppItem?> {
            return arrayOfNulls(size)
        }
    }


}

