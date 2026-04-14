package com.example.carebridge.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.example.carebridge.ml.NutriscanModel
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

    private fun getCenterCroppedBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = if (width > height) height else width
        val x = (width - size) / 2
        val y = (height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    private fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val cm = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, 0f,
            0f, contrast, 0f, 0f, 0f,
            0f, 0f, contrast, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return resultBitmap
    }

    fun classify(bitmap: Bitmap): ClassificationResult? {
        val currentModel = model ?: return null
        
        try {
            // Pre-process using Bitmap operations for steps not available in standard TFLite Support ops
            val croppedBitmap = getCenterCroppedBitmap(bitmap)
            val highContrastBitmap = adjustContrast(croppedBitmap, 1.2f)

            // Final preprocessing using ImageProcessor
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f)) 
                .build()

            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(highContrastBitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // 2. Run inference using the generated model binding
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
