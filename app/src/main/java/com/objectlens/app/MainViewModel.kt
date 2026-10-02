package com.objectlens.app

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.objectlens.app.data.HistoryEntry
import com.objectlens.app.data.HistoryRepository
import com.objectlens.app.ml.ImageClassifier
import com.objectlens.app.ml.Prediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetectState(
    val bitmap: Bitmap? = null,
    val predictions: List<Prediction> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HistoryRepository(app)
    private val classifier by lazy { ImageClassifier(app) }

    private val _detect = MutableStateFlow(DetectState())
    val detect: StateFlow<DetectState> = _detect.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) { _history.value = repo.load() }
    }

    fun onImagePicked(uri: Uri) {
        _detect.value = DetectState(loading = true)
        viewModelScope.launch {
            try {
                val (bitmap, preds) = withContext(Dispatchers.Default) {
                    val bmp = decode(uri)
                    bmp to classifier.classify(bmp)
                }
                _detect.value = DetectState(bitmap = bitmap, predictions = preds)
                withContext(Dispatchers.IO) {
                    repo.add(bitmap, preds)
                    _history.value = repo.load()
                }
            } catch (e: Exception) {
                _detect.value = DetectState(error = e.message ?: "Could not analyse this image")
            }
        }
    }

    fun clearResult() {
        _detect.value = DetectState()
    }

    fun delete(id: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.delete(id)
        _history.value = repo.load()
    }

    fun clearHistory() = viewModelScope.launch(Dispatchers.IO) {
        repo.clear()
        _history.value = repo.load()
    }

    private fun decode(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(getApplication<Application>().contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // pixels must be readable
            val longest = maxOf(info.size.width, info.size.height)
            if (longest > MAX_SIDE) {
                val scale = MAX_SIDE.toFloat() / longest
                decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
            }
        }
    }

    override fun onCleared() {
        runCatching { classifier.close() }
    }

    private companion object {
        const val MAX_SIDE = 1280
    }
}
