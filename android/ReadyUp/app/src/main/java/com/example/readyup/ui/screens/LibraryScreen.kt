package com.example.readyup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.readyup.data.model.StatusItem
import com.example.readyup.ui.components.*
import com.example.readyup.ui.theme.*
import com.example.readyup.viewmodel.LibraryViewModel

private val STATUS_TABS = listOf(
    "PLAYING" to "Jugando",
    "BACKLOG" to "Pendiente",
    "COMPLETED" to "Completado",
    "DROPPED" to "Abandonado"
)

@Composable
fun LibraryScreen(vm: LibraryViewModel, onGameClick: (Int) -> Unit) {
    val state by vm.libraryState.collectAsState()

    LaunchedEffect(Unit) { vm.loadLibrary("PLAYING") }

    Column(Modifier.fillMaxSize().background(BgDark)) {
        // Status tabs
        Row(
            Modifier
                .fillMaxWidth()
                .background(Surface)
                .border(1.dp, BorderColor)
        ) {
            STATUS_TABS.forEach { (statusVal, label) ->
                val isActive = state.currentStatus == statusVal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { vm.loadLibrary(statusVal) }
                        .background(if (isActive) Surface2 else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) Cyan else TextMuted
                        )
                        if (isActive) {
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(20.dp).height(2.dp).background(Cyan))
                        }
                    }
                }
            }
        }

        when {
            state.isLoading -> LoadingBox(Modifier.fillMaxSize())
            state.error != null -> EmptyState(state.error!!, Modifier.fillMaxSize())
            state.items.isEmpty() -> EmptyState("No tienes juegos aquí", Modifier.fillMaxSize())
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "${state.items.size} juego${if (state.items.size != 1) "s" else ""}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(state.items) { item ->
                        LibraryItem(item, onGameClick)
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryItem(item: StatusItem, onGameClick: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardBg)
            .border(1.dp, BorderColor)
            .clickable { onGameClick(item.game.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cover
        Box(
            Modifier
                .width(44.dp)
                .height(58.dp)
                .background(Surface2)
        ) {
            if (item.game.coverUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.game.coverUrl).crossfade(true).build(),
                    contentDescription = item.game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("🎮", fontSize = 18.sp)
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(item.game.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, maxLines = 2)
            Spacer(Modifier.height(3.dp))
            Text("Actualizado: ${formatDate(item.updatedAt)}", fontSize = 11.sp, color = TextMuted)
        }
        StatusChip(item.status)
    }
}
