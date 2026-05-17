package dev.anyfn

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test that runs on an emulator/device and asserts the app's
 * package id is the one we shipped. If this fails the entire build is wrong.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @Test
    fun applicationId() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(true, context.packageName.startsWith("dev.anyfn"))
    }
}
