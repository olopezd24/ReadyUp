package com.example.readyup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readyup.data.model.FeedItem
import com.example.readyup.ui.components.*
import com.example.readyup.ui.theme.*
import com.example.readyup.viewmodel.LibraryViewModel

@Composable
fun FeedScreen(vm: LibraryViewModel, onGameClick: (Int) -> Unit) {
    val state by vm.feedState.collectAsState()

    LaunchedEffect(Unit) { vm.loadFeed() }

    Column(Modifier.fillMaxSize().background(BgDark)) {
        when {
            state.isLoading -> LoadingBox(Modifier.fillMaxSize())
            state.error != null -> EmptyState(state.error!!, Modifier.fillMaxSize())
            state.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Tu feed está vacío", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("Sigue a otros usuarios para ver sus reseñas", fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("${state.items.size} reseñas recientes", fontSize = 11.sp, color = TextMuted, letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(4.dp))
                    }
                    items(state.items) { item -> FeedCard(item, onGameClick) }
                }
            }
        }
    }
}

@Composable
fun FeedCard(item: FeedItem, onGameClick: (Int) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardBg)
            .border(1.dp, BorderColor)
            .padding(14.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(item.user.username, 36)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(item.user.username, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                Text(
                    item.game.title,
                    fontSize = 12.sp,
                    color = Cyan,
                    modifier = Modifier.clickable { onGameClick(item.game.id) }
                )
            }
            Box(
                Modifier
                    .background(Lime.copy(alpha = 0.1f))
                    .border(1.dp, Lime.copy(alpha = 0.4f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("●${item.rating}/10", color = Lime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Review text
        if (item.text.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(item.text, fontSize = 14.sp, color = TextPrimary, lineHeight = 21.sp)
        }

        Spacer(Modifier.height(8.dp))
        Text(formatDate(item.updatedAt), fontSize = 11.sp, color = TextMuted)
    }
}
