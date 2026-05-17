/**
 * UIAutomatorBridge
 *
 * Thin wrapper around `androidx.test.uiautomator.UiDevice` so action steps
 * read declaratively in [ActionExecutor]. Selectors are mapped from the
 * core [Selector] type into UI Automator's `UiSelector`.
 *
 * UI Automator is the primary actuation surface because it works
 * cross-process without needing to inject anything into the target app.
 */
package dev.anyfn.invoker

import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import dev.anyfn.core.model.Selector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UIAutomatorBridge @Inject constructor() {

    private val device: UiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    fun launch(packageName: String, context: Context) {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: error("No launch intent for $packageName")
        launch.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
    }

    fun pressBack(): Boolean = device.pressBack()
    fun pressEnter(): Boolean = device.pressEnter()
    fun pressHome(): Boolean = device.pressHome()

    fun find(selector: Selector, timeoutMs: Long = 5_000L): UiObject2? =
        device.wait(Until.findObject(selector.toBy()), timeoutMs)

    fun click(selector: Selector, timeoutMs: Long = 5_000L): Boolean {
        val node = find(selector, timeoutMs) ?: return false
        node.click()
        return true
    }

    fun typeText(selector: Selector, text: String, timeoutMs: Long = 5_000L): Boolean {
        val node = find(selector, timeoutMs) ?: return false
        node.text = text
        return true
    }

    fun scroll(direction: dev.anyfn.core.model.ScrollDirection, steps: Int = 6): Boolean {
        val w = device.displayWidth
        val h = device.displayHeight
        return when (direction) {
            dev.anyfn.core.model.ScrollDirection.DOWN -> device.swipe(w / 2, (h * 0.75).toInt(), w / 2, (h * 0.25).toInt(), steps)
            dev.anyfn.core.model.ScrollDirection.UP -> device.swipe(w / 2, (h * 0.25).toInt(), w / 2, (h * 0.75).toInt(), steps)
            dev.anyfn.core.model.ScrollDirection.LEFT -> device.swipe((w * 0.75).toInt(), h / 2, (w * 0.25).toInt(), h / 2, steps)
            dev.anyfn.core.model.ScrollDirection.RIGHT -> device.swipe((w * 0.25).toInt(), h / 2, (w * 0.75).toInt(), h / 2, steps)
        }
    }

    fun waitForIdle(timeoutMs: Long = 3_000L) {
        device.waitForIdle(timeoutMs)
    }

    fun currentPackage(): String? = device.currentPackageName

    fun deviceInfo(): String = buildString {
        append("uiautomator on android ")
        append(Build.VERSION.SDK_INT)
        append(", display ")
        append(device.displayWidth).append("x").append(device.displayHeight)
    }

    @Suppress("UnusedPrivateMember")
    private fun Selector.toBy(): BySelector {
        val base: BySelector = when {
            byResourceId != null -> By.res(byResourceId)
            byText != null -> By.text(byText)
            byContentDescription != null -> By.desc(byContentDescription)
            byClassName != null -> By.clazz(byClassName)
            else -> error("empty selector")
        }
        return base
    }

    @Suppress("UnusedPrivateMember")
    private fun Selector.toUiSelector(): UiSelector {
        val s = UiSelector()
        return when {
            byResourceId != null -> s.resourceId(byResourceId)
            byText != null -> s.text(byText)
            byContentDescription != null -> s.description(byContentDescription)
            byClassName != null -> s.className(byClassName)
            else -> error("empty selector")
        }
    }
}
