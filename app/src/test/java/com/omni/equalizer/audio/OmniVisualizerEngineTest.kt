package com.omni.equalizer.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OmniVisualizerEngineTest {

    @Test
    fun `bucketFft returns exactly 10 bands regardless of input size`() {
        val silence = ByteArray(1024) // all zeros -> silence
        val bands = OmniVisualizerEngine.bucketFft(silence)
        assertEquals(10, bands.size)
    }

    @Test
    fun `bucketFft on silence produces all-zero bands`() {
        val silence = ByteArray(1024)
        val bands = OmniVisualizerEngine.bucketFft(silence)
        bands.forEach { level -> assertEquals(0f, level, 0.001f) }
    }

    @Test
    fun `bucketFft on strong signal produces normalised non-zero bands`() {
        // Fabricate a byte array with large magnitude components everywhere.
        val loud = ByteArray(1024) { 127 }
        val bands = OmniVisualizerEngine.bucketFft(loud)
        bands.forEach { level ->
            assertTrue("expected band to be > 0 for a loud signal", level > 0f)
            assertTrue("expected band to stay within normalised 0..1 range", level <= 1f)
        }
    }

    @Test
    fun `levels flow starts at all-zero before any real capture happens`() {
        // Guards against ever reverting to fabricated/animated placeholder data: with no
        // audio captured yet, the flow must be a real flatline, not a fake wiggle.
        val initial = OmniVisualizerEngine.levels.value
        assertEquals(10, initial.size)
        initial.forEach { assertEquals(0f, it, 0.001f) }
    }
}
