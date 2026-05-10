package com.example.readyup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.readyup.ui.theme.*

// ── Avatar initials ───────────────────────────────────────────────────────────

@Composable
fun InitialsAvatar(username: String, size: Int = 36) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                Brush.linearGradientBrush(
                    colors = listOf(Cyan, Rose)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.firstOrNull()?.uppercase() ?: "?",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.4f).sp
        )
    }
}

private fun Brush.Companion.linearGradientBrush(colors: List<Color>) =
    linearGradient(colors = colors)

// ── Game cover card ───────────────────────────────────────────────────────────

@Composable
fun GameCard(
    title: String,
    coverUrl: String?,
    releaseYear: String?,
    rating: Double?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .background(Surface2),
                contentAlignment = Alignment.Center
            ) {
                if (coverUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(coverUrl).crossfade(true).build(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("🎮", fontSize = 36.sp)
                }
                if (rating != null && rating > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "★ ${"%.1f".format(rating)}",
                            color = Lime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
                if (!releaseYear.isNullOrBlank()) {
                    Text(
                        releaseYear,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ── Rating dot picker ─────────────────────────────────────────────────────────

@Composable
fun RatingPicker(rating: Int, onRatingChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..10) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i <= rating) Lime else Surface2)
                    .border(1.dp, if (i <= rating) Lime else BorderColor, RoundedCornerShape(2.dp))
                    .clickable { onRatingChange(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$i",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (i <= rating) Color.Black else TextMuted
                )
            }
        }
    }
}

// ── Status chip ───────────────────────────────────────────────────────────────

fun statusLabel(s: String) = when (s) {
    "PLAYING" -> "Jugando"
    "BACKLOG" -> "Pendiente"
    "COMPLETED" -> "Completado"
    "DROPPED" -> "Abandonado"
    else -> s
}

@Composable
fun StatusChip(status: String) {
    val (bg, fg) = when (status) {
        "PLAYING" -> Lime.copy(alpha = 0.15f) to Lime
        "COMPLETED" -> Cyan.copy(alpha = 0.15f) to Cyan
        "DROPPED" -> Rose.copy(alpha = 0.15f) to Rose
        else -> Color.White.copy(alpha = 0.05f) to TextMuted
    }
    Box(
        modifier = Modifier
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            statusLabel(status).uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = fg
        )
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(text: String) {
    Column {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Divider(color = BorderColor, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))
    }
}

// ── Loading / empty states ────────────────────────────────────────────────────

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Cyan, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(message, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
