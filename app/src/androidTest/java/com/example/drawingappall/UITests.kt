package com.example.drawingappall

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingappall.views.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // Fix here

    @Test
    fun testSplashScreen() {
        composeTestRule.onNodeWithContentDescription("Paint Icon").assertIsDisplayed()
    }

    @Test
    fun testNavigateToDrawingScreen() {
        // Click "Let's Draw" to navigate
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()

        // Wait for the new screen to load
        composeTestRule.waitForIdle()

        // Check if any element specific to the drawing screen exists
        composeTestRule.onNodeWithContentDescription("Circle Button").assertExists()
    }

    @Test
    fun testSelectShapes() {
        // Click the "Let's Draw" button to navigate
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()

        // Wait for the new screen to load
        composeTestRule.waitForIdle()

        // Now check for and click shape buttons
        composeTestRule.onNodeWithContentDescription("Circle Button").assertExists().performClick()
        composeTestRule.onNodeWithContentDescription("Square Button").assertExists().performClick()
        composeTestRule.onNodeWithContentDescription("Triangle Button").assertExists().performClick()
    }

    @Test
    fun testPickColorButton() {
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()
        composeTestRule.waitForIdle()

        // Click the color picker button
        composeTestRule.onNodeWithText("Change Color").assertExists().performClick()
    }

    @Test
    fun testUpdateBrushSizeWithSlider() {
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()
        composeTestRule.waitForIdle()

        // Find the slider and increase size
        composeTestRule.onNodeWithTag("BrushSizeSlider").assertExists()
            .performTouchInput { swipeRight() } // Increase size

        // Find the slider and decrease size
        composeTestRule.onNodeWithTag("BrushSizeSlider").assertExists()
            .performTouchInput { swipeLeft() } // Decrease size
    }
}