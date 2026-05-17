/**
 * ScannerViewModel — thin wrapper around [ScanScheduler]'s state.
 */
package dev.anyfn.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anyfn.scanner.ScanProgress
import dev.anyfn.scanner.ScanScheduler
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scheduler: ScanScheduler,
) : ViewModel() {

    val progress: StateFlow<ScanProgress> = scheduler.progress

    fun startScan() {
        scheduler.startFullScan()
    }

    fun cancel() {
        viewModelScope.launch { scheduler.cancel() }
    }
}
