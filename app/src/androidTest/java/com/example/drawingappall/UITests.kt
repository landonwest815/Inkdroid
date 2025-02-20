package com.example.drawingappall

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSplashScreen() {

        composeTestRule.setContent {
            SplashScreen ({})
        }
        composeTestRule.onNodeWithContentDescription("Paint Icon").assertIsDisplayed()

    }
}