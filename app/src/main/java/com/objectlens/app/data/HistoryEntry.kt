package com.objectlens.app.data

import com.objectlens.app.ml.Prediction

data class HistoryEntry(
    val id: String,
    val timestamp: Long,
    val imagePath: String,
    val predictions: List<Prediction>
) {
    val top: Prediction? get() = predictions.firstOrNull()
}
