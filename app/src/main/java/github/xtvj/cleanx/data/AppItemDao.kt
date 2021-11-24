package github.xtvj.cleanx.data

import androidx.room.*

@Dao
interface AppItemDao {

    @Query("SELECT * FROM appItem")
    fun getAll(): List<AppItem>

    @Query("SELECT * FROM appItem WHERE isSystem = 0 ORDER BY id ASC")
    fun getUser(): List<AppItem>

    @Query("SELECT * FROM appItem WHERE isSystem = 1 ORDER BY id ASC")
    fun getSystem(): List<AppItem>

    @Query("SELECT * FROM appItem WHERE isEnable = 0 ORDER BY id ASC")
    fun getDisable(): List<AppItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg appItem: AppItem)

    @Update
    suspend fun updateItem(vararg users: AppItem)

    @Query("SELECT * FROM appItem WHERE name LIKE :search OR id LIKE :search")
    fun findWithNameOrId(search: String): List<AppItem>

    @Delete
    suspend fun delete(appItem: AppItem)

    @Query("DELETE FROM appItem")
    suspend fun deleteAll()

}