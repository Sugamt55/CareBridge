package com.example.carebridge.data.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    private val inputImageSize = 224
    private val modelPath = "nutriscan_model.tflite"
    private val labelPath = "labels.txt"

    private val TAG = "TFLiteClassifier"

    init {
        try {
            Log.d(TAG, "Initializing TFLite Interpreter...")
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)
            labels = FileUtil.loadLabels(context, labelPath)
            Log.d(TAG, "Successfully loaded model: $modelPath and labels: $labelPath")
            Log.d(TAG, "Number of labels: ${labels.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TFLite model or labels: ${e.message}", e)
        }
    }

    fun isModelLoaded(): Boolean = interpreter != null && labels.isNotEmpty()

    fun classify(bitmap: Bitmap): ClassificationResult? {
        val interpreter = interpreter ?: run {
            Log.e(TAG, "Classifier not initialized")
            return null
        }
        
        try {
            // 1. Preprocess the image
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputImageSize, inputImageSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f)) 
                .build()

            var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // 2. Run inference
            // Get output shape from interpreter to be dynamic if possible, 
            // but for now sticking to the provided 1x16 structure with error checking.
            val outputShape = interpreter.getOutputTensor(0).shape() // [1, 16] or similar
            val numClasses = outputShape[1]
            
            val outputBuffer = ByteBuffer.allocateDirect(1 * numClasses * 4).order(ByteOrder.nativeOrder())
            interpreter.run(tensorImage.buffer, outputBuffer)

            // 3. Post-process the output
            outputBuffer.rewind()
            val confidences = FloatArray(numClasses)
            outputBuffer.asFloatBuffer().get(confidences)

            var maxIndex = -1
            var maxConfidence = 0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxIndex = i
                }
            }

            return if (maxIndex != -1 && maxIndex < labels.size) {
                Log.d(TAG, "Prediction: ${labels[maxIndex]} with confidence $maxConfidence")
                ClassificationResult(
                    label = labels[maxIndex],
                    confidence = maxConfidence
                )
            } else {
                Log.w(TAG, "Max index $maxIndex out of bounds for labels size ${labels.size}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during classification: ${e.message}", e)
            return null
        }
    }

    data class ClassificationResult(
        val label: String,
        val confidence: Float
    )

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
