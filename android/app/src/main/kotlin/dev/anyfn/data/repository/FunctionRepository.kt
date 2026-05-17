/**
 * FunctionRepository
 *
 * The single place the rest of the app talks to about discovered functions.
 * Translates between the JSON-flattened [AppFunctionEntity] and the domain
 * [AppFunction] so callers never see Room or JSON strings.
 */
package dev.anyfn.data.repository

import dev.anyfn.core.model.AppFunction
import dev.anyfn.core.model.ParameterSchema
import dev.anyfn.core.model.UiAction
import dev.anyfn.data.db.AppFunctionDao
import dev.anyfn.data.db.AppFunctionEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Singleton
class FunctionRepository @Inject constructor(
    private val dao: AppFunctionDao,
    private val json: Json,
) {

    fun observeAll(): Flow<List<AppFunction>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observeEnabled(): Flow<List<AppFunction>> =
        dao.observeEnabled().map { rows -> rows.map { it.toDomain() } }

    fun count(): Flow<Int> = dao.count()

    suspend fun byId(id: Long): AppFunction? = dao.byId(id)?.toDomain()

    suspend fun byName(name: String): AppFunction? = dao.byName(name)?.toDomain()

    suspend fun upsert(function: AppFunction): Long = dao.upsert(function.toEntity())

    suspend fun upsertAll(functions: List<AppFunction>): List<Long> =
        dao.upsertAll(functions.map { it.toEntity() })

    suspend fun setEnabled(id: Long, enabled: Boolean): Unit = dao.setEnabled(id, enabled)

    suspend fun delete(id: Long): Unit = dao.delete(id)

    suspend fun deleteByPackage(packageName: String): Unit = dao.deleteByPackage(packageName)

    private fun AppFunctionEntity.toDomain(): AppFunction = AppFunction(
        name = name,
        packageName = packageName,
        appLabel = appLabel,
        description = description,
        parameters = json.decodeFromString(ListSerializer(ParameterSchema.serializer()), parametersJson),
        uiPath = json.decodeFromString(ListSerializer(UiAction.serializer()), uiPathJson),
        confidence = confidence,
        requiresReview = requiresReview,
        destructive = destructive,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )

    private fun AppFunction.toEntity(): AppFunctionEntity = AppFunctionEntity(
        name = name,
        packageName = packageName,
        appLabel = appLabel,
        description = description,
        parametersJson = json.encodeToString(ListSerializer(ParameterSchema.serializer()), parameters),
        uiPathJson = json.encodeToString(ListSerializer(UiAction.serializer()), uiPath),
        confidence = confidence,
        requiresReview = requiresReview,
        destructive = destructive,
        enabled = true,
        createdAtMillis = if (createdAtMillis == 0L) System.currentTimeMillis() else createdAtMillis,
        updatedAtMillis = System.currentTimeMillis(),
    )
}
