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
import com.example.nutriscanai.ui.theme.nutriscanaiTheme
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

    // Bypasses the OS permission dialog
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

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
            nutriscanaiTheme {
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
            try {
                composeTestRule.onNodeWithText("AI Report Scanner").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        val captureButton = composeTestRule.onNodeWithContentDescription("Capture", useUnmergedTree = true)
        captureButton.assertIsDisplayed()

        // --- STEP 2: TRIGGER CAPTURE & VERIFY LOADING ---
        captureButton.performClick()
        
        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule.onNodeWithText("Analyzing Food...").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

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
            macronutrients = Macronutrients(0.5, 25.0, 0.3, 4.4, 19.0),
            micronutrients = Micronutrients(0.1, 8.4, 1.0, 0.1, 195.0),
            phClassification = "Acidic",
            phReason = "High in malic acid."
        )

        uiStateFlow.value = ScanUiState.Success(dummyResponse, dummyDetailedData)

        // Wait for Bottom Sheet content
        composeTestRule.waitUntil(15000) {
            try {
                composeTestRule.onNodeWithText("Apple", useUnmergedTree = true).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Scroll to and verify integrated data nodes
        composeTestRule.onNodeWithText("Acidic", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Potassium: 195.0mg", useUnmergedTree = true).performScrollTo().assertIsDisplayed()

        // --- STEP 4: DISMISSAL ---
        composeTestRule.onNodeWithContentDescription("Close", useUnmergedTree = true).performClick()
        
        // Return ViewModel state to Idle
        uiStateFlow.value = ScanUiState.Idle
        
        // Wait for sheet content to be completely removed from hierarchy
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Apple", useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
        }
        
        // Final verification: Ensure main screen revealed and interactable
        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule.onNodeWithText("AI Report Scanner").assertIsDisplayed()
                composeTestRule.onNodeWithContentDescription("Capture", useUnmergedTree = true).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
