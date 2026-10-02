package com.objectlens.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.objectlens.app.DetectState
import com.objectlens.app.MainViewModel

@Composable
fun DetectScreen(vm: MainViewModel) {
    val state by vm.detect.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.onImagePicked(uri)
    }
    val pick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Header()
        when {
            state.loading -> LoadingCard()
            state.bitmap != null -> ResultContent(state, onNew = pick, onClear = vm::clearResult)
            else -> EmptyCard(state.error, onPick = pick)
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text("ObjectLens", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text(
            "Identify cars, dogs, cats and 1,000 other things",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EmptyCard(error: String?, onPick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Filled.AddPhotoAlternate, null, Modifier.size(72.dp), tint = androidx.compose.ui.graphics.Color.White)
        Text(
            "Upload a photo to get started",
            style = MaterialTheme.typography.titleLarge,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Powered by MobileNet V2 running fully on your device. Nothing leaves your phone.",
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        if (error != null) {
            Text(error, color = androidx.compose.ui.graphics.Color(0xFFFFE4E6), textAlign = TextAlign.Center)
        }
        Button(
            onClick = onPick,
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.PhotoLibrary, null)
            Spacer(Modifier.width(8.dp))
            Text("Choose image")
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(Modifier.fillMaxWidth().height(260.dp), shape = RoundedCornerShape(28.dp)) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Analysing image…")
        }
    }
}

@Composable
private fun ResultContent(state: DetectState, onNew: () -> Unit, onClear: () -> Unit) {
    val bmp: Bitmap = state.bitmap!!
    Image(
        bitmap = bmp.asImageBitmap(),
        contentDescription = "Selected image",
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 340.dp)
            .clip(RoundedCornerShape(28.dp)),
        contentScale = ContentScale.Crop
    )
    state.predictions.firstOrNull()?.let { top ->
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Best match", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(top.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                CategoryChip(top.category)
            }
        }
    }
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Top predictions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            PredictionList(state.predictions)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onNew, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.PhotoLibrary, null)
            Spacer(Modifier.width(8.dp))
            Text("New image")
        }
        OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Clear") }
    }
    Text(
        "Saved to history automatically.",
        Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}
