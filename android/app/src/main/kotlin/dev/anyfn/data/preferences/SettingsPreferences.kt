/**
 * SettingsPreferences
 *
 * DataStore-backed settings that the user changes from the Settings screen.
 * Exposes everything as Flow so screens recompose when the user flips a knob.
 */
package dev.anyfn.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("anyfn_settings")

enum class LlmProvider { ANTHROPIC, GEMINI_NANO }

data class SettingsSnapshot(
    val anthropicApiKey: String = "",
    val provider: LlmProvider = LlmProvider.ANTHROPIC,
    val mcpPort: Int = DEFAULT_MCP_PORT,
    val mcpSharedSecret: String = "",
    val mcpLanMode: Boolean = false,
    val confirmDestructive: Boolean = true,
    val includeSystemApps: Boolean = false,
    val debugMode: Boolean = false,
) {
    companion object {
        const val DEFAULT_MCP_PORT: Int = 5174
    }
}

@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val ANTHROPIC_KEY: Preferences.Key<String> = stringPreferencesKey("anthropic_api_key")
        val PROVIDER: Preferences.Key<String> = stringPreferencesKey("llm_provider")
        val MCP_PORT: Preferences.Key<Int> = intPreferencesKey("mcp_port")
        val MCP_SECRET: Preferences.Key<String> = stringPreferencesKey("mcp_secret")
        val MCP_LAN: Preferences.Key<Boolean> = booleanPreferencesKey("mcp_lan")
        val CONFIRM_DESTRUCTIVE: Preferences.Key<Boolean> = booleanPreferencesKey("confirm_destructive")
        val INCLUDE_SYSTEM_APPS: Preferences.Key<Boolean> = booleanPreferencesKey("include_system_apps")
        val DEBUG: Preferences.Key<Boolean> = booleanPreferencesKey("debug_mode")
    }

    val snapshot: Flow<SettingsSnapshot> = context.settingsDataStore.data.map { prefs ->
        SettingsSnapshot(
            anthropicApiKey = prefs[Keys.ANTHROPIC_KEY].orEmpty(),
            provider = runCatching { LlmProvider.valueOf(prefs[Keys.PROVIDER] ?: LlmProvider.ANTHROPIC.name) }
                .getOrDefault(LlmProvider.ANTHROPIC),
            mcpPort = prefs[Keys.MCP_PORT] ?: SettingsSnapshot.DEFAULT_MCP_PORT,
            mcpSharedSecret = prefs[Keys.MCP_SECRET].orEmpty(),
            mcpLanMode = prefs[Keys.MCP_LAN] ?: false,
            confirmDestructive = prefs[Keys.CONFIRM_DESTRUCTIVE] ?: true,
            includeSystemApps = prefs[Keys.INCLUDE_SYSTEM_APPS] ?: false,
            debugMode = prefs[Keys.DEBUG] ?: false,
        )
    }

    suspend fun setAnthropicApiKey(value: String): Unit =
        context.settingsDataStore.edit { it[Keys.ANTHROPIC_KEY] = value }.let { }

    suspend fun setProvider(value: LlmProvider): Unit =
        context.settingsDataStore.edit { it[Keys.PROVIDER] = value.name }.let { }

    suspend fun setMcpPort(port: Int): Unit =
        context.settingsDataStore.edit { it[Keys.MCP_PORT] = port }.let { }

    suspend fun setMcpSharedSecret(secret: String): Unit =
        context.settingsDataStore.edit { it[Keys.MCP_SECRET] = secret }.let { }

    suspend fun setMcpLanMode(enabled: Boolean): Unit =
        context.settingsDataStore.edit { it[Keys.MCP_LAN] = enabled }.let { }

    suspend fun setConfirmDestructive(value: Boolean): Unit =
        context.settingsDataStore.edit { it[Keys.CONFIRM_DESTRUCTIVE] = value }.let { }

    suspend fun setIncludeSystemApps(value: Boolean): Unit =
        context.settingsDataStore.edit { it[Keys.INCLUDE_SYSTEM_APPS] = value }.let { }

    suspend fun setDebugMode(value: Boolean): Unit =
        context.settingsDataStore.edit { it[Keys.DEBUG] = value }.let { }
}
