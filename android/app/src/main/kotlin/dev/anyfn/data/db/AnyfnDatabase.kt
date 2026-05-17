/**
 * Room database. v0.1 ships with one entity (functions) and one history
 * table (scan runs). Future migrations live in [Migrations].
 */
package dev.anyfn.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppFunctionEntity::class, ScanRunEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AnyfnDatabase : RoomDatabase() {
    abstract fun appFunctionDao(): AppFunctionDao
    abstract fun scanRunDao(): ScanRunDao

    companion object {
        const val NAME: String = "anyfn.db"
    }
}
