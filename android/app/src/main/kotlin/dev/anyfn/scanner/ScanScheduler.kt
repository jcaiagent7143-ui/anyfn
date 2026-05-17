/**
 * ScanScheduler
 *
 * Owns the lifecycle of a "Scan device" operation:
 *   - lists user-installed apps via [AppDiscovery]
 *   - for each app, asks [FunctionInferrer] to produce its function list
 *   - writes results into [FunctionRepository]
 *   - emits [ScanProgress] events the UI subscribes to
 *
 * Runs in its own scope owned by the singleton — cancelling the scan only
 * requires calling [cancel]; the UI never has to manage Job references.
 */
package dev.anyfn.scanner

import dev.anyfn.data.db.ScanRunDao
import dev.anyfn.data.db.ScanRunEntity
import dev.anyfn.data.preferences.SettingsPreferences
import dev.anyfn.data.repository.FunctionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ScanProgress {
    data object Idle : ScanProgress
    data class Running(
        val currentIndex: Int,
        val totalApps: Int,
        val currentApp: String,
        val functionsSoFar: Int,
    ) : ScanProgress
    data class Done(val appsScanned: Int, val functionsFound: Int, val errors: List<String>) : ScanProgress
    data class Failed(val message: String) : ScanProgress
}

@Singleton
class ScanScheduler @Inject constructor(
    private val discovery: AppDiscovery,
    private val inferrer: FunctionInferrer,
    private val repository: FunctionRepository,
    private val scanRuns: ScanRunDao,
    private val settings: SettingsPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
    private val _progress = MutableStateFlow<ScanProgress>(ScanProgress.Idle)
    val progress: StateFlow<ScanProgress> = _progress.asStateFlow()

    private var currentJob: Job? = null

    fun startFullScan(): Boolean {
        if (currentJob?.isActive == true) return false
        currentJob = scope.launch {
            runScan(null)
        }
        return true
    }

    fun rescan(packageName: String): Boolean {
        if (currentJob?.isActive == true) return false
        currentJob = scope.launch { runScan(packageName) }
        return true
    }

    suspend fun cancel() {
        currentJob?.cancelAndJoin()
        if (_progress.value is ScanProgress.Running) {
            _progress.value = ScanProgress.Failed("cancelled")
        }
    }

    private suspend fun runScan(onlyPackage: String?) {
        val snapshot = settings.snapshot.first()
        val all = discovery.listInstalledApps(includeSystem = snapshot.includeSystemApps)
        val targets = if (onlyPackage == null) all else all.filter { it.packageName == onlyPackage }
        if (targets.isEmpty()) {
            _progress.value = ScanProgress.Failed("no apps to scan")
            return
        }

        val runId = scanRuns.upsert(
            ScanRunEntity(
                startedAtMillis = System.currentTimeMillis(),
                finishedAtMillis = null,
                appsScanned = 0,
                functionsFound = 0,
                status = "running",
            ),
        )

        val errors = mutableListOf<String>()
        var functionsFound = 0

        targets.forEachIndexed { idx, app ->
            _progress.value = ScanProgress.Running(
                currentIndex = idx + 1,
                totalApps = targets.size,
                currentApp = app.label,
                functionsSoFar = functionsFound,
            )
            val r = inferrer.inferFor(app)
            r.fold(
                onSuccess = { functions ->
                    repository.upsertAll(functions)
                    functionsFound += functions.size
                },
                onFailure = { e ->
                    errors += "${app.label}: ${e.message ?: "unknown error"}"
                },
            )
        }

        scanRuns.finish(
            runId,
            finishedAt = System.currentTimeMillis(),
            apps = targets.size,
            functions = functionsFound,
            status = if (errors.isEmpty()) "ok" else "partial",
        )

        _progress.value = ScanProgress.Done(
            appsScanned = targets.size,
            functionsFound = functionsFound,
            errors = errors,
        )
    }

    @Suppress("unused")
    fun shutdown(): Unit = scope.cancel()
}
