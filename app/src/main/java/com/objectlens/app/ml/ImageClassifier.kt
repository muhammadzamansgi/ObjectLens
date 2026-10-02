package com.objectlens.app.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Runs the quantized MobileNetV2 (ImageNet, 1001 classes incl. "background") model.
 * Input: 224x224 RGB uint8. Output: 1001 uint8 scores (scale 1/255).
 */
class ImageClassifier(context: Context) : Closeable {

    private val labels: List<String> =
        context.assets.open(LABELS_FILE).bufferedReader().readLines().map { prettify(it) }
    private val interpreter: Interpreter
    private val input: ByteBuffer =
        ByteBuffer.allocateDirect(SIZE * SIZE * 3).order(ByteOrder.nativeOrder())

    init {
        val fd = context.assets.openFd(MODEL_FILE)
        val model: MappedByteBuffer = fd.use {
            java.io.FileInputStream(it.fileDescriptor).channel
                .map(FileChannel.MapMode.READ_ONLY, it.startOffset, it.declaredLength)
        }
        interpreter = Interpreter(model, Interpreter.Options().setNumThreads(4))
    }

    @Synchronized
    fun classify(bitmap: Bitmap, topK: Int = 5): List<Prediction> {
        val square = centerCrop(bitmap)
        val scaled = Bitmap.createScaledBitmap(square, SIZE, SIZE, true)
        val pixels = IntArray(SIZE * SIZE)
        scaled.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE)

        input.rewind()
        for (p in pixels) {
            input.put(((p shr 16) and 0xFF).toByte())
            input.put(((p shr 8) and 0xFF).toByte())
            input.put((p and 0xFF).toByte())
        }
        input.rewind()

        val output = Array(1) { ByteArray(labels.size) }
        interpreter.run(input, output)

        return output[0].mapIndexed { i, b -> i to (b.toInt() and 0xFF) / 255f }
            .filter { it.first != 0 } // index 0 is "background"
            .sortedByDescending { it.second }
            .take(topK)
            .map { Prediction(labels[it.first], it.second) }
    }

    private fun centerCrop(src: Bitmap): Bitmap {
        val side = minOf(src.width, src.height)
        return Bitmap.createBitmap(src, (src.width - side) / 2, (src.height - side) / 2, side, side)
    }

    private fun prettify(raw: String) =
        raw.trim().split(' ').joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }

    override fun close() = interpreter.close()

    companion object {
        private const val MODEL_FILE = "mobilenet_v2_1.0_224_quant.tflite"
        private const val LABELS_FILE = "labels.txt"
        private const val SIZE = 224
    }
}
