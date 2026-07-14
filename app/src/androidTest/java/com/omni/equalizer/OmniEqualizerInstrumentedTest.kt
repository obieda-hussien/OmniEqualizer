package com.omni.equalizer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Basic instrumented sanity checks. Replaces the previous unmodified `com.example`
 * template test, which asserted on a package name ("com.example") that didn't even
 * belong to this app.
 */
@RunWith(AndroidJUnit4::class)
class OmniEqualizerInstrumentedTest {

    @Test
    fun appContext_hasCorrectPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.omni.equalizer", appContext.packageName)
    }
}
