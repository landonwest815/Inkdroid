package com.example.drawingappall

import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
        // Click "Let's Draw" to go to gallery
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()

        // Tap the FAB to create a new drawing and navigate to draw screen
        composeTestRule.onNodeWithContentDescription("New Drawing").assertExists().performClick()

        // Wait for shape buttons to load to confirm we're on the draw screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("CircleButton").fetchSemanticsNodes().isNotEmpty()
        }

        // Confirm we’re on the Draw screen
        composeTestRule.onNodeWithTag("CircleButton").assertExists()
    }

    @Test
    fun testSelectShapes() {
        // Navigate to gallery
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()

        // Create a new drawing (goes to draw screen)
        composeTestRule.onNodeWithContentDescription("New Drawing").assertExists().performClick()

        // Wait for shape buttons to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("CircleButton").fetchSemanticsNodes().isNotEmpty()
        }

        // Interact with shape buttons
        composeTestRule.onNodeWithTag("CircleButton").performClick()
        composeTestRule.onNodeWithTag("SquareButton").performClick()
        composeTestRule.onNodeWithTag("TriangleButton").performClick()
    }

    @Test
    fun testPickColorButton() {
        // Navigate to gallery
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()

        // Tap the "New Drawing" FAB to enter draw screen
        composeTestRule.onNodeWithContentDescription("New Drawing").assertExists().performClick()

        // Wait for the "Change Color" button to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Change Color").fetchSemanticsNodes().isNotEmpty()
        }

        // Tap the "Change Color" button
        composeTestRule.onNodeWithText("Change Color").performClick()
    }

    @Test
    fun testUpdateBrushSizeWithSlider() {
        // Navigate to Gallery
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()

        // Tap "New Drawing" FAB to open Draw Screen
        composeTestRule.onNodeWithContentDescription("New Drawing").assertExists().performClick()

        // Wait for slider to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("BrushSizeSlider").fetchSemanticsNodes().isNotEmpty()
        }

        // Increase brush size
        composeTestRule.onNodeWithTag("BrushSizeSlider")
            .performTouchInput { swipeRight() }

        // Decrease brush size
        composeTestRule.onNodeWithTag("BrushSizeSlider")
            .performTouchInput { swipeLeft() }
    }

    @Test
    fun testNavigateToGalleryFromSplash() {
        // From splash to gallery
        composeTestRule.onNodeWithText("Let's Draw").assertExists().performClick()

        // Confirm header
        composeTestRule.onNodeWithText("My Drawings").assertExists()

        // Confirm FAB to create a new drawing
        composeTestRule.onNodeWithContentDescription("New Drawing").assertExists()
    }

    @Test
    fun testCreateNewDrawingNavigatesToDrawScreen() {
        composeTestRule.onNodeWithText("Let's Draw").performClick()
        composeTestRule.onNodeWithContentDescription("New Drawing").performClick()

        // Drawing screen loaded — verify something unique like shape buttons or slider
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("Circle Button").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testDrawingAppearsInGalleryAfterSaveAndClose() {
        composeTestRule.onNodeWithText("Let's Draw").performClick()
        composeTestRule.onNodeWithContentDescription("New Drawing").performClick()

        // Wait and tap back button (which saves and closes)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))[0].assertExists()
    }

    @Test
    fun testRenameDrawing() {
        // Go to gallery and create new drawing
        composeTestRule.onNodeWithText("Let's Draw").performClick()
        composeTestRule.onNodeWithContentDescription("New Drawing").performClick()

        // Tap filename to open rename dialog
        composeTestRule.onNodeWithTag("FileNameDisplay").assertExists().performClick()

        // Enter new name in text field
        composeTestRule.onNodeWithTag("RenameInput").assertExists().performTextClearance()
        composeTestRule.onNodeWithTag("RenameInput").performTextInput("RenamedDrawing")

        // Confirm rename
        composeTestRule.onNodeWithTag("RenameConfirm").performClick()

        // Assert new name now displayed in header
        composeTestRule.onNodeWithText("RenamedDrawing").assertExists()
    }

    @Test
    fun testRenameDrawingGalleryPermanence() {
        // Go to gallery and create new drawing
        composeTestRule.onNodeWithText("Let's Draw").performClick()
        composeTestRule.onNodeWithContentDescription("New Drawing").performClick()

        // Tap filename to open rename dialog
        composeTestRule.onNodeWithTag("FileNameDisplay").performClick()

        // Enter new name in text field
        composeTestRule.onNodeWithTag("RenameInput").performTextClearance()
        composeTestRule.onNodeWithTag("RenameInput").performTextInput("RenamedDrawing")

        // Confirm rename
        composeTestRule.onNodeWithTag("RenameConfirm").performClick()

        // Save + Close and return to gallery
        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        composeTestRule.onNodeWithTag("FileName_RenamedDrawing").assertExists()
    }

    @Test
    fun testDeleteDrawingFromGallery() {
        // Navigate to gallery and create drawing
        composeTestRule.onNodeWithText("Let's Draw").performClick()
        composeTestRule.onNodeWithContentDescription("New Drawing").performClick()
        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        // Wait until a drawing appears
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Extract first drawing's filename from tag
        val tag = composeTestRule
            .onAllNodes(hasTestTagStartingWith("DrawingCard_"))[0]
            .fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsProperties.TestTag]

        val fileName = tag.removePrefix("DrawingCard_")

        // Tap delete button using Delete_$fileName tag
        composeTestRule.onNodeWithTag("Delete_$fileName").assertExists().performClick()
    }

    fun hasTestTagStartingWith(prefix: String): SemanticsMatcher {
        return SemanticsMatcher("TestTag starts with '$prefix'") { node ->
            val tag = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)
            tag?.startsWith(prefix) == true
        }
    }
}