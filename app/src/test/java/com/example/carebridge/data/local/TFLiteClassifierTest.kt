package com.example.carebridge.data.local

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.carebridge.ml.NutriscanModel
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], manifest = Config.NONE)
class TFLiteClassifierTest {

    private lateinit var classifier: TFLiteClassifier
    private lateinit var context: Context
    private val mockModel = mockk<NutriscanModel>(relaxed = true)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Mock the static newInstance method of NutriscanModel 
        // to avoid loading native TFLite libraries on the JVM
        mockkStatic(NutriscanModel::class)
        every { NutriscanModel.newInstance(any()) } returns mockModel
        
        classifier = TFLiteClassifier(context)
    }

    @Test
    fun `isModelLoaded returns true when model is mocked`() {
        // TFLiteClassifier.init calls NutriscanModel.newInstance which we mocked
        assertTrue("Model should be considered loaded", classifier.isModelLoaded())
    }

    @Test
    fun `classify handles preprocessing and inference without native crash`() {
        // 1. Arrange: Setup the mock to return a dummy output
        val mockOutput = mockk<NutriscanModel.Outputs>()
        val dummyBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 16), DataType.FLOAT32)
        dummyBuffer.loadArray(FloatArray(16) { 0.1f }) // Flat confidence
        
        every { mockModel.process(any()) } returns mockOutput
        every { mockOutput.outputFeature0AsTensorBuffer } returns dummyBuffer

        val inputBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)

        // 2. Act
        val result = classifier.classify(inputBitmap)

        // 3. Assert
        assertNotNull("Result should not be null", result)
        // Verify that the model was actually called (proving preprocessing finished)
        verify { mockModel.process(any()) }
    }
}
