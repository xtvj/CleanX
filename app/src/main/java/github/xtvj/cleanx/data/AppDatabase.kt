package github.xtvj.cleanx.data

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = arrayOf(AppItem::class), version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun appItemDao(): AppItemDao
}