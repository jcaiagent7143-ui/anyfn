/**
 * InferenceModule — selects an LLM backend based on the user's settings.
 *
 * We expose a [LlmBackend] singleton wrapped behind a provider so the active
 * backend can be swapped at runtime (e.g. user enters an Anthropic key after
 * starting in Gemini Nano mode) without rebuilding the Hilt graph.
 */
package dev.anyfn.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.anyfn.data.preferences.SettingsPreferences
import dev.anyfn.inference.AnthropicBackend
import dev.anyfn.inference.GeminiNanoBackend
import dev.anyfn.inference.LlmBackend
import dev.anyfn.inference.LlmBackendSelector
import dev.anyfn.inference.PromptLibrary
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InferenceModule {

    @Provides
    @Singleton
    fun providePromptLibrary(@ApplicationContext context: Context): PromptLibrary =
        PromptLibrary(context)

    @Provides
    @Singleton
    fun provideAnthropicBackend(prompts: PromptLibrary): AnthropicBackend =
        AnthropicBackend(prompts)

    @Provides
    @Singleton
    fun provideGeminiNanoBackend(
        @ApplicationContext context: Context,
        prompts: PromptLibrary,
    ): GeminiNanoBackend = GeminiNanoBackend(context, prompts)

    @Provides
    @Singleton
    fun provideBackend(
        settings: SettingsPreferences,
        anthropic: AnthropicBackend,
        gemini: GeminiNanoBackend,
    ): LlmBackend = LlmBackendSelector(settings, anthropic, gemini)
}
