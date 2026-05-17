/**
 * Room entity mirroring [dev.anyfn.core.model.AppFunction].
 *
 * Complex fields (parameters, ui_path) are stored as serialized JSON columns;
 * the conversion lives in [Converters]. This keeps the schema flat and trivial
 * to migrate while still allowing structured editing through repositories.
 */
package dev.anyfn.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_functions",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["packageName"]),
    ],
)
data class AppFunctionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val packageName: String,
    val appLabel: String,
    val description: String,
    @ColumnInfo(name = "parameters_json") val parametersJson: String,
    @ColumnInfo(name = "ui_path_json") val uiPathJson: String,
    val confidence: Double,
    val requiresReview: Boolean,
    val destructive: Boolean,
    val enabled: Boolean = true,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
