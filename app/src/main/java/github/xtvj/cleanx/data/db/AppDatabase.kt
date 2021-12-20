package github.xtvj.cleanx.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import github.xtvj.cleanx.data.dao.AppItemDao
import github.xtvj.cleanx.data.entity.AppItem
import github.xtvj.cleanx.utils.DATABASE_NAME


@Database(entities = arrayOf(AppItem::class), version = 2,exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun appItemDao(): AppItemDao


    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        val MIGRATION_1_2 = Migration(1, 2) {
//            it.execSQL("****************")
        }


        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
//                .addMigrations(MIGRATION_1_2)
//                .addCallback(
//                    object : RoomDatabase.Callback() {
//                        override fun onCreate(db: SupportSQLiteDatabase) {
//                            super.onCreate(db)
//                            val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
//                                .setInputData(workDataOf(KEY_FILENAME to PLANT_DATA_FILENAME))
//                                .build()
//                            WorkManager.getInstance(context).enqueue(request)
//                        }
//                    }
//                )
                .build()
        }


    }
}