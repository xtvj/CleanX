package github.xtvj.cleanx.data

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface AppItemDao {

    @Query("SELECT * FROM appItem")
    fun getAll(): PagingSource<Int,AppItem>

    @Query("SELECT * FROM appItem WHERE isSystem = 0 ORDER BY CASE WHEN :sortByColumn = 'id' AND :sortDirection = 1 THEN id END ASC,CASE WHEN :sortByColumn = 'id' AND :sortDirection = 0 THEN id END DESC,CASE WHEN :sortByColumn = 'name' AND :sortDirection = 1 THEN name COLLATE NOCASE END ASC,CASE WHEN :sortByColumn = 'name' AND :sortDirection = 0 THEN name COLLATE NOCASE END DESC,CASE WHEN :sortByColumn = 'lastUpdateTime' AND :sortDirection = 1 THEN lastUpdateTime END ASC,CASE WHEN :sortByColumn = 'lastUpdateTime' AND :sortDirection = 0 THEN lastUpdateTime END DESC")
    fun getUser(sortByColumn: String, sortDirection: Boolean): PagingSource<Int, AppItem>

    @Query("SELECT * FROM appItem WHERE isSystem = 1 ORDER BY CASE WHEN :sortByColumn = 'id' AND :sortDirection = 1 THEN id END ASC,CASE WHEN :sortByColumn = 'id' AND :sortDirection = 0 THEN id END DESC,CASE WHEN :sortByColumn = 'name' AND :sortDirection = 1 THEN name COLLATE NOCASE END ASC,CASE WHEN :sortByColumn = 'name' AND :sortDirection = 0 THEN name COLLATE NOCASE END DESC,CASE WHEN :sortByColumn = 'lastUpdateTime' AND :sortDirection = 1 THEN lastUpdateTime END ASC,CASE WHEN :sortByColumn = 'lastUpdateTime' AND :sortDirection = 0 THEN lastUpdateTime END DESC")
    fun getSystem(sortByColumn: String, sortDirection: Boolean): PagingSource<Int,AppItem>

    @Query("SELECT * FROM appItem WHERE isEnable = 0 ORDER BY CASE WHEN :sortByColumn = 'id' AND :sortDirection = 1 THEN id END ASC,CASE WHEN :sortByColumn = 'id' AND :sortDirection = 0 THEN id END DESC,CASE WHEN :sortByColumn = 'name' AND :sortDirection = 1 THEN name COLLATE NOCASE END ASC,CASE WHEN :sortByColumn = 'name' AND :sortDirection = 0 THEN name COLLATE NOCASE END DESC,CASE WHEN :sortByColumn = 'lastUpdateTime' AND :sortDirection = 1 THEN lastUpdateTime END ASC,CASE WHEN :sortByColumn = 'lastUpdateTime' AND :sortDirection = 0 THEN lastUpdateTime END DESC")
    fun getDisable(sortByColumn: String, sortDirection: Boolean): PagingSource<Int,AppItem>

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