package com.objectlens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.objectlens.app.ml.Category
import com.objectlens.app.ml.Prediction
import java.text.DateFormat
import java.util.Date

fun Category.icon(): ImageVector = when (this) {
    Category.DOG, Category.CAT, Category.ANIMAL -> Icons.Filled.Pets
    Category.VEHICLE -> Icons.Filled.DirectionsCar
    Category.OBJECT -> Icons.Filled.Category
}

fun Category.color(): Color = when (this) {
    Category.DOG -> Color(0xFFF59E0B)
    Category.CAT -> Color(0xFFEC4899)
    Category.ANIMAL -> Color(0xFF10B981)
    Category.VEHICLE -> Color(0xFF0EA5E9)
    Category.OBJECT -> Color(0xFF8B5CF6)
}

fun formatTime(ts: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(ts))

fun percent(c: Float) = "${(c * 100).toInt()}%"

@Composable
fun CategoryChip(category: Category, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(category.color().copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(category.icon(), null, Modifier.size(14.dp), tint = category.color())
        Spacer(Modifier.width(4.dp))
        Text(category.title, style = MaterialTheme.typography.labelMedium, color = category.color())
    }
}

@Composable
fun PredictionList(predictions: List<Prediction>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        predictions.forEachIndexed { i, p ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        p.label,
                        Modifier.weight(1f),
                        style = if (i == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(percent(p.confidence), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { p.confidence.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
