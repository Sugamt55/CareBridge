package com.example.carebridge

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carebridge.data.FoodPredictionResponse
import com.example.carebridge.data.model.FoodDatabaseItem
import com.example.carebridge.data.model.Macronutrients
import com.example.carebridge.data.model.Micronutrients
import com.example.carebridge.ui.screens.ScanScreen
import com.example.carebridge.ui.theme.CareBridgeTheme
import com.example.carebridge.ui.viewmodel.MainViewModel
import com.example.carebridge.ui.viewmodel.ScanUiState
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

    private lateinit var uiStateFlow: MutableStateFlow<ScanUiState>
    private lateinit var mockViewModel: MainViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<MainViewModel>(relaxed = true)
        uiStateFlow = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
        every { mockViewModel.uiState } returns uiStateFlow
    }

    @Test
    fun testEndToEndScanFlow() {
        composeTestRule.setContent {
            CareBridgeTheme {
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

        // --- STEP 1: INITIAL STATE ---
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("AI Report Scanner", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("AI Report Scanner", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Capture", useUnmergedTree = true).assertIsDisplayed()

        // --- STEP 2: TRIGGER CAPTURE & VERIFY LOADING ---
        composeTestRule.onNodeWithContentDescription("Capture", useUnmergedTree = true).performClick()
        
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Analyzing Food...", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Analyzing Food...", useUnmergedTree = true).assertIsDisplayed()

        // --- STEP 3: SIMULATE SUCCESS (RESULT DISPLAY) ---
        val dummyResponse = FoodPredictionResponse(
            foodName = "Apple",
            servingSize = "100g",
            calories = 52,
            protein = "0.3g",
            carbs = "14g",
            fat = "0.2g",
            phLevel = 3.5,
            isAlkaline = false
        )
        
        val dummyDetailedData = FoodDatabaseItem(
            foodName = "Apple",
            servingSize = "1 medium (182g)",
            calories = 95,
            macronutrients = Macronutrients(
                proteinG = 0.5, 
                carbsG = 25.0, 
                fatG = 0.3, 
                fiberG = 4.4, 
                sugarG = 19.0
            ),
            micronutrients = Micronutrients(
                vitaminAIu = 0.1,
                vitaminCMg = 8.4,
                calciumMg = 1.0,
                ironMg = 0.1,
                potassiumMg = 195.0
            ),
            phClassification = "Acidic",
            phReason = "High in malic acid."
        )

        uiStateFlow.value = ScanUiState.Success(dummyResponse, dummyDetailedData)

        // Wait for the Bottom Sheet to appear
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Apple", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        
        // Wait for it to be actually displayed (animation check)
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithText("Apple", useUnmergedTree = true).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Scroll to and verify integrated data
        composeTestRule.onNodeWithText("Acidic", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Potassium: 195.0mg", useUnmergedTree = true).performScrollTo().assertIsDisplayed()

        // --- STEP 4: DISMISSAL ---
        composeTestRule.onNodeWithContentDescription("Close", useUnmergedTree = true).performClick()
        
        // Manually reset state as our mock won't do it automatically via production code path
        uiStateFlow.value = ScanUiState.Idle
        
        // Wait for sheet content to be completely removed from the tree
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Apple", useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
        }
        
        // Final verification: Ensure main screen revealed and interactable
        // We use a longer wait and catch errors to handle the final dismissal animation/scrim cleanup
        composeTestRule.waitUntil(15000) {
            try {
                // Check both elements are back and visible to the user
                composeTestRule.onNodeWithText("AI Report Scanner", useUnmergedTree = true).assertIsDisplayed()
                composeTestRule.onNodeWithContentDescription("Capture", useUnmergedTree = true).assertIsDisplayed()
                true
            } catch (e: Throwable) {
                false
            }
        }
    }
}
