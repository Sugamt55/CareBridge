package com.example.nutriscanai

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-420dpi", instrumentedPackages = ["androidx.loader.content"])
class ScanFlowLocalTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var uiStateFlow: MutableStateFlow<ScanUiState>
    private lateinit var mockViewModel: MainViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<MainViewModel>(relaxed = true)
        uiStateFlow = MutableStateFlow(ScanUiState.Idle)
        every { mockViewModel.uiState } returns uiStateFlow
    }

    @Test
    fun testEndToEndScanFlowLocal() {
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
        composeTestRule.onNodeWithText("AI Food Scanner").assertExists()
        
        val captureButton = composeTestRule.onNodeWithContentDescription("Capture", useUnmergedTree = true)
        captureButton.assertExists()

        // 2. Trigger Capture & Verify Loading
        captureButton.performClick()
        
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Analyzing Food...", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Analyzing Food...", substring = true).assertExists()

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

        // 4. Verify result display
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Apple", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Apple", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Acidic", useUnmergedTree = true).performScrollTo().assertExists()

        // 5. Dismiss Bottom Sheet
        composeTestRule.onNodeWithContentDescription("Close", useUnmergedTree = true).performClick()
        uiStateFlow.value = ScanUiState.Idle
        
        // Final verification
        composeTestRule.onNodeWithText("AI Food Scanner").assertExists()
    }
}
