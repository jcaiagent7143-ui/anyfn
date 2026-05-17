/**
 * PromptLibrary — loads prompt templates from the `assets/prompts/` directory.
 * Caches the read result in-process; prompts are small enough that an LRU is
 * overkill.
 */
package dev.anyfn.inference

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

class PromptLibrary(private val context: Context) {

    private val cache = ConcurrentHashMap<String, String>()

    fun load(name: String): String = cache.getOrPut(name) {
        context.assets.open("prompts/$name").bufferedReader().use { it.readText() }
    }

    fun render(name: String, vars: Map<String, String>): String {
        var template = load(name)
        for ((k, v) in vars) template = template.replace("{$k}", v)
        return template
    }
}
