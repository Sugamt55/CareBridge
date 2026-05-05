package com.example.nutriscanai.data.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.nutriscanai.ml.NutriscanModel
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.DataType

class TFLiteClassifier(private val context: Context) {

    private var model: NutriscanModel? = null
    private var labels: List<String> = emptyList()
    private val labelPath = "labels.txt"
    private val TAG = "TFLiteClassifier"

    init {
        try {
            model = NutriscanModel.newInstance(context)
            labels = FileUtil.loadLabels(context, labelPath)
            Log.i(TAG, "LiteRT: Model binding initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "LiteRT: Failed to initialize model binding", e)
        }
    }

    fun isModelLoaded(): Boolean = model != null && labels.isNotEmpty()

    fun classify(bitmap: Bitmap): ClassificationResult? {
        val currentModel = model ?: return null
        
        try {
            // Updated Pre-processing: 
            // 1. Removed aggressive crop and contrast boost to match API behavior
            // 2. Updated Normalization to [-1, 1] to match EfficientNetV2 training
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(127.5f, 127.5f)) // Results in (x - 127.5) / 127.5 -> [-1, 1]
                .build()

            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // 2. Run inference
            val outputs = currentModel.process(tensorImage.tensorBuffer)
            val confidences = outputs.outputFeature0AsTensorBuffer.floatArray

            // 3. Post-process
            var maxIndex = -1
            var maxConfidence = 0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxIndex = i
                }
            }

            return if (maxIndex != -1 && maxIndex < labels.size) {
                Log.d(TAG, "LiteRT: Prediction -> ${labels[maxIndex]} ($maxConfidence)")
                ClassificationResult(label = labels[maxIndex], confidence = maxConfidence)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "LiteRT: Inference failed", e)
            return null
        }
    }

    data class ClassificationResult(val label: String, val confidence: Float)

    fun close() {
        model?.close()
        model = null
    }
}
