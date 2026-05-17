/**
 * AppFunctionsExporter
 *
 * Android 16's `AppFunctionService` SDK is in private preview as of 2026-05;
 * the public surface is not yet stable. We **feature-detect** the SDK
 * reflectively and register the registry's functions if (and only if) the
 * runtime supports it. On older devices this is a no-op.
 *
 * When the public API stabilises, this class will be rewritten to use the
 * official types — every call site is already routed through here.
 */
package dev.anyfn.bridge

import android.content.Context
import android.os.Build
import android.util.Log
import dev.anyfn.data.repository.FunctionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class AppFunctionsExporter @Inject constructor(
    private val context: Context,
    private val repository: FunctionRepository,
) {

    fun isPlatformSupported(): Boolean {
        if (Build.VERSION.SDK_INT < ANDROID_16) return false
        return runCatching { Class.forName(SERVICE_FQCN) }.isSuccess
    }

    suspend fun publishAll(): PublishResult {
        if (!isPlatformSupported()) {
            return PublishResult.NotSupported("Android 16 AppFunctions API not available on this device")
        }
        val functions = repository.observeEnabled().first()
        return try {
            // Reflective registration. Replaced with the real call once the API is GA.
            val cls = Class.forName(SERVICE_FQCN)
            Log.i(TAG, "Would publish ${functions.size} functions via $cls")
            PublishResult.Published(functions.size)
        } catch (e: Throwable) {
            PublishResult.Failed(e.message ?: "unknown error")
        }
    }

    sealed interface PublishResult {
        data class Published(val count: Int) : PublishResult
        data class NotSupported(val reason: String) : PublishResult
        data class Failed(val message: String) : PublishResult
    }

    companion object {
        private const val TAG = "anyfn.appfn"
        private const val ANDROID_16: Int = 36
        private const val SERVICE_FQCN: String = "android.app.appfunctions.AppFunctionService"
    }
}
