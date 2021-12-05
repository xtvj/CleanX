package github.xtvj.cleanx.data

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface AppItemDao {

    @Query("SELECT * FROM appItem")
    fun getAll(): PagingSource<Int,AppItem>

    @Query("SELECT * FROM appItem WHERE isSystem = 0 ORDER BY id ASC")
    fun getUser(): PagingSource<Int,AppItem>

    @Query("SELECT * FROM appItem WHERE isSystem = 1 ORDER BY id ASC")
    fun getSystem(): PagingSource<Int,AppItem>

    @Query("SELECT * FROM appItem WHERE isEnable = 0 ORDER BY id ASC")
    fun getDisable(): PagingSource<Int,AppItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg appItem: AppItem)

    @Update
    suspend fun updateItem(vararg users: AppItem)

    /**
     * Updating only isEnable
     * By order id
     */
    @Query("UPDATE appItem SET isEnable = :b WHERE id =:appID")
    suspend fun update(appID:String, b:Boolean)

    @Query("SELECT * FROM appItem WHERE name LIKE :search OR id LIKE :search")
    fun findWithNameOrId(search: String): PagingSource<Int,AppItem>

    @Delete
    suspend fun delete(appItem: AppItem)

    @Query("DELETE FROM appItem")
    suspend fun deleteAll()

    @Query("DELETE FROM appItem WHERE isSystem = 0")
    suspend fun deleteAllUser()

    @Query("DELETE FROM appItem WHERE isSystem = 1")
    suspend fun deleteAllSystem()

    @Query("DELETE FROM appItem WHERE isEnable = 0")
    suspend fun deleteAllDisable()

}