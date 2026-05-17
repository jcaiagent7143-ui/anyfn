/**
 * OnboardingPreferences — tracks whether the user has finished the first-run flow.
 * Split out from [SettingsPreferences] so the root nav graph doesn't have to
 * pull in the entire settings surface just to gate one boolean.
 */
package dev.anyfn.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingStore by preferencesDataStore("anyfn_onboarding")

@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val key: Preferences.Key<Boolean> = booleanPreferencesKey("completed")

    val completed: Flow<Boolean> = context.onboardingStore.data.map { it[key] ?: false }

    suspend fun setCompleted(value: Boolean): Unit =
        context.onboardingStore.edit { it[key] = value }.let { }
}
