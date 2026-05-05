package com.example.nutriscanai

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.nutriscanai.data.FoodPredictionResponse
import com.example.nutriscanai.data.model.FoodDatabaseItem
import com.example.nutriscanai.data.model.Macronutrients
import com.example.nutriscanai.data.model.Micronutrients
import com.example.nutriscanai.ui.screens.ScanScreen
import com.example.nutriscanai.ui.theme.NutriScanAITheme
import com.example.nutriscanai.ui.viewmodel.MainViewModel
import com.example.nutriscanai.ui.viewmodel.ScanUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScanFlowIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    private lateinit var uiStateFlow: MutableStateFlow<ScanUiState>
    private lateinit var mockViewModel: MainViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<MainViewModel>(relaxed = true)
        uiStateFlow = MutableStateFlow(ScanUiState.Idle)
        every { mockViewModel.uiState } returns uiStateFlow
    }

    @Test
    fun testEndToEndScanFlow() {
        composeTestRule.setContent {
            NutriScanAITheme {
                ScanScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel,
                    isTestMode = true,
                    onTestCapture = {
                        uiStateFlow.value = ScanUiState.Loading
                    }
                )
            }
        }

        // 1. Initial State
        composeTestRule.onNodeWithText("AI Food Scanner").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Capture").performClick()

        // 2. Verify Loading (Wait for transition)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Analyzing Food...", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Simulate AI Result
        val dummyResponse = FoodPredictionResponse(
            foodName = "Apple", servingSize = "100g", calories = 52,
            protein = "0.3g", carbs = "14g", fat = "0.2g",
            phLevel = 3.5, isAlkaline = false
        )

        val dummyDetailedData = FoodDatabaseItem(
            foodName = "Apple", servingSize = "1 medium", calories = 95,
            macronutrients = Macronutrients(0.5, 25.0, 0.3, 4.4, 19.0),
            micronutrients = Micronutrients(0.1, 8.4, 1.0, 0.1, 195.0),
            phClassification = "Acidic",
            phReason = "High in malic acid."
        )

        uiStateFlow.value = ScanUiState.Success(dummyResponse, dummyDetailedData)

        // 4. Verify Results (Wait for BottomSheet to finish animation)
        composeTestRule.waitUntil(20000) {
            try {
                // Ensure the sheet is actually visible before asserting
                composeTestRule.onNodeWithText("Apple", useUnmergedTree = true).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Wait specifically for the "Acidic" tag to be displayed (accounting for internal layout lag)
        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule.onNode(hasText("Acidic", substring = false), useUnmergedTree = true).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // 5. Dismiss BottomSheet
        composeTestRule.onNodeWithContentDescription("Close", useUnmergedTree = true).performClick()
        uiStateFlow.value = ScanUiState.Idle

        // 6. Final verification: Wait for sheet to vanish and home screen to reveal
        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule.onNodeWithText("AI Food Scanner").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}