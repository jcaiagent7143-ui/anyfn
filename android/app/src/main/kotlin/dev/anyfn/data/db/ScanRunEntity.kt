/**
 * Persisted record of a scan run, for the history shown in the home screen.
 */
package dev.anyfn.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "scan_runs")
data class ScanRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val startedAtMillis: Long,
    val finishedAtMillis: Long?,
    val appsScanned: Int,
    val functionsFound: Int,
    val status: String,
)

@Dao
interface ScanRunDao {
    @Query("SELECT * FROM scan_runs ORDER BY startedAtMillis DESC LIMIT 10")
    fun recent(): Flow<List<ScanRunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScanRunEntity): Long

    @Query("UPDATE scan_runs SET finishedAtMillis = :finishedAt, appsScanned = :apps, functionsFound = :functions, status = :status WHERE id = :id")
    suspend fun finish(id: Long, finishedAt: Long, apps: Int, functions: Int, status: String)
}
