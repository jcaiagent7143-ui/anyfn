/**
 * DAO for the function registry. Read APIs return Flow; writes are suspend.
 */
package dev.anyfn.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppFunctionDao {

    @Query("SELECT * FROM app_functions ORDER BY appLabel, name")
    fun observeAll(): Flow<List<AppFunctionEntity>>

    @Query("SELECT * FROM app_functions WHERE enabled = 1 ORDER BY appLabel, name")
    fun observeEnabled(): Flow<List<AppFunctionEntity>>

    @Query("SELECT * FROM app_functions WHERE id = :id")
    suspend fun byId(id: Long): AppFunctionEntity?

    @Query("SELECT * FROM app_functions WHERE name = :name LIMIT 1")
    suspend fun byName(name: String): AppFunctionEntity?

    @Query("SELECT COUNT(*) FROM app_functions")
    fun count(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppFunctionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AppFunctionEntity>): List<Long>

    @Update
    suspend fun update(entity: AppFunctionEntity)

    @Query("UPDATE app_functions SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM app_functions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM app_functions WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)
}
