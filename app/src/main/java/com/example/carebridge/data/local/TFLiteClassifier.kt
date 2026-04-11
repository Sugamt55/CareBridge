package com.example.carebridge.data.local

import android.content.Context
import android.graphics.Bitmap
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

    init {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            interpreter = Interpreter(model)
            labels = FileUtil.loadLabels(context, labelPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult? {
        val interpreter = interpreter ?: return null
        
        // 1. Preprocess the image
        // Resize to 224x224 and normalize to 0-1
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageSize, inputImageSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) 
            .build()

        var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Run inference
        // Output shape is [1, 16]
        val outputBuffer = ByteBuffer.allocateDirect(1 * 16 * 4).order(ByteOrder.nativeOrder())
        interpreter.run(tensorImage.buffer, outputBuffer)

        // 3. Post-process the output
        outputBuffer.rewind()
        val confidences = FloatArray(16)
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
            ClassificationResult(
                label = labels[maxIndex],
                confidence = maxConfidence
            )
        } else {
            null
        }
    }

    data class ClassificationResult(
        val label: String,
        val confidence: Float
    )

    fun close() {
        interpreter?.close()
    }
}
