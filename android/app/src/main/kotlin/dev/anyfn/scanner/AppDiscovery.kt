/**
 * AppDiscovery — enumerates installed apps the user is likely to want exposed.
 *
 * Default policy: user-installed only (no system flag). Apps without a
 * launcher intent are filtered out — those have no UI for us to scan.
 *
 * The system-app toggle in settings flips this; if a user really wants to
 * call into Files or Settings, anyfn will not stop them.
 */
package dev.anyfn.scanner

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class InstalledApp(
    val packageName: String,
    val label: String,
    val launchIntent: Intent,
    val isSystem: Boolean,
)

class AppDiscovery @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun listInstalledApps(includeSystem: Boolean = false): List<InstalledApp> {
        val pm = context.packageManager
        val flags = PackageManager.GET_META_DATA
        val infos = pm.getInstalledApplications(flags)
        return infos.mapNotNull { info ->
            val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystem && !includeSystem) return@mapNotNull null
            val launch = pm.getLaunchIntentForPackage(info.packageName) ?: return@mapNotNull null
            InstalledApp(
                packageName = info.packageName,
                label = pm.getApplicationLabel(info).toString(),
                launchIntent = launch.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                isSystem = isSystem,
            )
        }.sortedBy { it.label.lowercase() }
    }

    fun launch(app: InstalledApp) {
        context.startActivity(app.launchIntent)
    }
}
