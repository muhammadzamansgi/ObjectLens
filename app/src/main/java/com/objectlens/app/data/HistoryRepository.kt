package com.objectlens.app.data

import android.content.Context
import android.graphics.Bitmap
import com.objectlens.app.ml.Prediction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Stores scan history as a JSON index plus one JPEG per scan in the app's private storage. */
class HistoryRepository(context: Context) {

    private val dir = File(context.filesDir, "history").apply { mkdirs() }
    private val index = File(dir, "history.json")

    @Synchronized
    fun load(): List<HistoryEntry> {
        if (!index.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(index.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val preds = o.getJSONArray("predictions")
                HistoryEntry(
                    id = o.getString("id"),
                    timestamp = o.getLong("timestamp"),
                    imagePath = o.getString("imagePath"),
                    predictions = (0 until preds.length()).map {
                        val p = preds.getJSONObject(it)
                        Prediction(p.getString("label"), p.getDouble("confidence").toFloat())
                    }
                )
            }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun add(bitmap: Bitmap, predictions: List<Prediction>): HistoryEntry {
        val id = UUID.randomUUID().toString()
        val file = File(dir, "$id.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        val entry = HistoryEntry(id, System.currentTimeMillis(), file.absolutePath, predictions)
        write(listOf(entry) + load())
        return entry
    }

    @Synchronized
    fun delete(id: String) {
        val all = load()
        all.firstOrNull { it.id == id }?.let { File(it.imagePath).delete() }
        write(all.filterNot { it.id == id })
    }

    @Synchronized
    fun clear() {
        load().forEach { File(it.imagePath).delete() }
        write(emptyList())
    }

    private fun write(entries: List<HistoryEntry>) {
        val arr = JSONArray()
        entries.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("timestamp", e.timestamp)
                put("imagePath", e.imagePath)
                put("predictions", JSONArray().apply {
                    e.predictions.forEach {
                        put(JSONObject().put("label", it.label).put("confidence", it.confidence.toDouble()))
                    }
                })
            })
        }
        index.writeText(arr.toString())
    }
}
