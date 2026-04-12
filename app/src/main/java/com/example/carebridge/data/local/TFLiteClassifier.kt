package com.example.carebridge.data.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var labels: List<String> = emptyList()

    private val inputImageSize = 224
    private val modelPath = "nutriscan_model.tflite"
    private val labelPath = "labels.txt"

    private val TAG = "TFLiteClassifier"

    init {
        try {
            Log.d(TAG, "Initializing TFLite Interpreter (Consolidated v2.16.1)...")
            
            // Check if asset exists first
            val assetList = context.assets.list("")?.toList() ?: emptyList()
            if (!assetList.contains(modelPath)) {
                Log.e(TAG, "CRITICAL: $modelPath not found in assets! Found: $assetList")
            }

            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options()
            
            // Try to add GPU Delegate for acceleration
            try {
                // Catch Throwable to handle NoClassDefFoundError which is an Error
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate)
                Log.d(TAG, "GPU Delegate initialized successfully")
            } catch (t: Throwable) {
                Log.w(TAG, "GPU Delegate failed (expected on some devices), falling back to CPU: ${t.message}")
                gpuDelegate = null
            }

            interpreter = Interpreter(model, options)
            labels = FileUtil.loadLabels(context, labelPath)
            
            Log.d(TAG, "Successfully loaded model: $modelPath and labels: $labelPath")
            Log.d(TAG, "Number of labels loaded: ${labels.size}")
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL ERROR during TFLite initialization: ${e.message}", e)
        }
    }

    fun isModelLoaded(): Boolean = interpreter != null && labels.isNotEmpty()

    fun classify(bitmap: Bitmap): ClassificationResult? {
        val currentInterpreter = interpreter ?: run {
            Log.e(TAG, "Classification failed: Interpreter is null")
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
            val outputShape = currentInterpreter.getOutputTensor(0).shape() 
            val numClasses = outputShape[1]
            
            val outputBuffer = ByteBuffer.allocateDirect(1 * numClasses * 4).order(ByteOrder.nativeOrder())
            currentInterpreter.run(tensorImage.buffer, outputBuffer)

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
                Log.d(TAG, "Local Prediction: ${labels[maxIndex]} ($maxConfidence)")
                ClassificationResult(
                    label = labels[maxIndex],
                    confidence = maxConfidence
                )
            } else {
                Log.w(TAG, "Prediction failed: maxIndex $maxIndex out of bounds")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during inference: ${e.message}", e)
            return null
        }
    }

    data class ClassificationResult(
        val label: String,
        val confidence: Float
    )

    fun close() {
        try {
            interpreter?.close()
            gpuDelegate?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing resources: ${e.message}")
        } finally {
            interpreter = null
            gpuDelegate = null
        }
    }
}
