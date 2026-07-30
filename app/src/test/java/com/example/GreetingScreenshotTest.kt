package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.SignalMetrics
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.GalaxyDSPTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dashboard_screenshot() {
    composeTestRule.setContent {
      GalaxyDSPTheme(darkTheme = true) {
        MainDashboardScreen(
          metrics = SignalMetrics(
            snrDb = 22.5f,
            evmRmsPercent = 4.1f,
            carrierLocked = true
          ),
          onSnrChanged = {},
          onFrequencyChanged = {},
          onTogglePipeline = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard.png")
  }
}
