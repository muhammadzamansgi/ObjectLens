package com.objectlens.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.objectlens.app.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(vm: MainViewModel, id: String, onBack: () -> Unit) {
    val items by vm.history.collectAsState()
    val entry = items.firstOrNull { it.id == id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    if (entry != null) {
                        IconButton(onClick = { vm.delete(entry.id); onBack() }) { Icon(Icons.Filled.Delete, "Delete") }
                    }
                }
            )
        }
    ) { padding ->
        if (entry == null) {
            Box(Modifier.fillMaxSize().padding(padding)) { Text("Scan not found", Modifier.padding(20.dp)) }
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                AsyncImage(
                    model = File(entry.imagePath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).clip(RoundedCornerShape(28.dp)),
                    contentScale = ContentScale.Crop
                )
                entry.top?.let {
                    Text(it.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    CategoryChip(it.category)
                }
                Text(formatTime(entry.timestamp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp)) { PredictionList(entry.predictions) }
                }
            }
        }
    }
}
