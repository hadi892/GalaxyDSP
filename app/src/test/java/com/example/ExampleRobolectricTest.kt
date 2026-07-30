package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.navigation.NavigationTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GalaxyDSP", appName)
  }

  @Test
  fun `verify navigation tabs definition`() {
    assertEquals(6, NavigationTab.entries.size)
    assertEquals("Dashboard", NavigationTab.DASHBOARD.title)
    assertEquals("Spectrum", NavigationTab.SPECTRUM.title)
    assertEquals("Waterfall", NavigationTab.WATERFALL.title)
    assertEquals("QPSK", NavigationTab.CONSTELLATION.title)
    assertEquals("Benchmark", NavigationTab.BENCHMARK.title)
    assertEquals("About", NavigationTab.ABOUT.title)
  }
}
