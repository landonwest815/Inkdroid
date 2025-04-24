package com.example.drawingappall

import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
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
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingappall.views.MainActivity
import kotlinx.coroutines.delay
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
        composeTestRule.onNodeWithTag("username_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("submit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_mode_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("error_text").assertIsNotDisplayed()
    }

    @Test
     fun testLoginLogout() {
        composeTestRule.onNodeWithContentDescription("Paint Icon").assertIsDisplayed()
        login()
        logout()
        composeTestRule.onNodeWithContentDescription("Paint Icon").assertIsDisplayed()
    }

    @Test
    fun testNavigateToDrawingScreen() {
       login()

        // Tap the FAB to create a new drawing and navigate to draw screen
        composeTestRule.onNodeWithContentDescription("New Drawing").assertExists().performClick()

        // Wait for shape buttons to load to confirm we're on the draw screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("CircleButton").fetchSemanticsNodes().isNotEmpty()
        }

        // Confirm we’re on the Draw screen
        composeTestRule.onNodeWithTag("CircleButton").assertExists()

        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        logout()
    }

    @Test
    fun testSelectShapes() {
        login()

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

        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        logout()
    }

    @Test
    fun testPickColorButton() {
        login()

        // Tap the "New Drawing" FAB to enter draw screen
        composeTestRule.onNodeWithContentDescription("New Drawing").assertExists().performClick()

        // Wait for the "Change Color" button to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Change Color").fetchSemanticsNodes().isNotEmpty()
        }

        // Tap the "Change Color" button
        composeTestRule.onNodeWithText("Change Color").performClick()

        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        logout()
    }

    @Test
    fun testUpdateBrushSizeWithSlider() {
        login()

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

        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        logout()
    }

    @Test
    fun testNavigateToGalleryFromSplash() {
        login()

        // Confirm header
        composeTestRule.onNodeWithText("My Drawings").assertExists()

        // Confirm FAB to create a new drawing
        composeTestRule.onNodeWithContentDescription("New Drawing").assertExists()

        logout()
    }

    @Test
    fun testCreateNewDrawingNavigatesToDrawScreen() {
        login()

        composeTestRule.onNodeWithContentDescription("New Drawing").performClick()

        // Drawing screen loaded — verify something unique like shape buttons or slider
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("Circle Button").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        logout()
    }

    @Test
    fun testDrawingAppearsInGalleryAfterSaveAndClose() {
        login()

        composeTestRule.onNodeWithContentDescription("New Drawing").performClick()

        // Wait and tap back button (which saves and closes)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        //wait for gallery to load
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("logout").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))[0].assertExists()

        composeTestRule.onNodeWithTag("logout").performClick()
    }

    @Test
    fun testRenameDrawing() {
        login()

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

        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        logout()
    }

    @Test
    fun testRenameDrawingGalleryPermanence() {
        login()
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

        logout()
    }

    @Test
    fun testNavigateToUpload() {
        login()
        composeTestRule.onNodeWithTag("tab_Uploaded").performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("owner_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        logout()
    }

    @Test
    fun testUpload() {
        login()
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

        composeTestRule.onNodeWithTag("Upload_${fileName}").assertExists().performClick()

        // Wait until a drawing appears
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("owner_test").assertIsDisplayed()

        composeTestRule.onNodeWithTag("DeleteServer_${fileName}").performClick()
        logout()
    }

    @Test
    fun testDownload() {
        login()
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

        composeTestRule.onNodeWithTag("Upload_${fileName}").assertExists().performClick()

        logout()

        login("test2")

        composeTestRule.onNodeWithTag("tab_Uploaded").performClick()

        // Wait until a drawing appears
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("Download_${fileName}").performClick()

        // Wait until a drawing appears
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_${fileName}"))[0]
            .assertExists().performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("Circle Button").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Save & Close").performClick()

        logout()

        login()

        composeTestRule.onNodeWithTag("tab_Uploaded").performClick()

        // Wait until a drawing appears
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("DeleteServer_${fileName}").performClick()

        logout()
    }

    @Test
    fun testDeleteDrawingFromGalleryLocal() {
        login()

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
        composeTestRule.onNodeWithTag("DeleteLocal_$fileName").assertExists().performClick()

        logout()
    }

    @Test
    fun testDeleteDrawingFromLocalGalleryPersistanceOnUploaded() {
        login()

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

        composeTestRule.onNodeWithTag("Upload_${fileName}").assertExists().performClick()

        composeTestRule.onNodeWithTag("tab_My Drawings").performClick()

        // Wait until a drawings appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Tap delete button using Delete_$fileName tag
        composeTestRule.onNodeWithTag("DeleteLocal_$fileName").assertExists().performClick()

        composeTestRule.onNodeWithTag("tab_Uploaded").performClick()

        // Wait until a drawings appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("DrawingCard_$fileName").assertIsNotDisplayed()

        logout()
    }

    @Test
    fun testDeleteDrawingFromUploadedGalleryPersistanceOnLocal(){
        login()

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

        composeTestRule.onNodeWithTag("Upload_${fileName}").assertExists().performClick()

        // Wait until a drawings appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_${fileName}"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Tap delete button using Delete_$fileName tag
        composeTestRule.onNodeWithTag("DeleteServer_${fileName}").performClick()

        composeTestRule.onNodeWithTag("tab_My Drawings").performClick()

        // Wait until a drawings appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasTestTagStartingWith("DrawingCard_"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("DrawingCard_$fileName").assertIsNotDisplayed()

        logout()
    }

    fun hasTestTagStartingWith(prefix: String): SemanticsMatcher {
        return SemanticsMatcher("TestTag starts with '$prefix'") { node ->
            val tag = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)
            tag?.startsWith(prefix) == true
        }
    }

    fun login(usernameAndPassword: String = "test"){
        composeTestRule.onNodeWithTag("username_field").performTextInput(usernameAndPassword)
        composeTestRule.onNodeWithTag("password_field").performTextInput(usernameAndPassword)
        composeTestRule.onNodeWithTag("submit").performClick()

        //wait for gallery to load
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("logout").fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun logout(){
        //wait for gallery to load
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("logout").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("logout").performClick()
    }
}