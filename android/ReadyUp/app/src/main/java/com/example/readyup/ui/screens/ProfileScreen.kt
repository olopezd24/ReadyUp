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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readyup.data.model.MyReviewItem
import com.example.readyup.ui.components.*
import com.example.readyup.ui.theme.*
import com.example.readyup.viewmodel.AuthViewModel
import com.example.readyup.viewmodel.LibraryViewModel

@Composable
fun ProfileScreen(
    authVm: AuthViewModel,
    libraryVm: LibraryViewModel,
    onGameClick: (Int) -> Unit
) {
    val authState by authVm.state.collectAsState()
    val profileState by libraryVm.profileState.collectAsState()

    LaunchedEffect(Unit) { libraryVm.loadProfile() }

    LazyColumn(
        Modifier.fillMaxSize().background(BgDark),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Profile header
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .border(1.dp, BorderColor)
            ) {
                // Accent top line
                Box(
                    Modifier.fillMaxWidth().height(2.dp)
                        .background(Brush.horizontalGradient(listOf(Cyan, Rose)))
                )
                Row(
                    Modifier.padding(16.dp).padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InitialsAvatar(authState.username, 56)
                    Column {
                        Text(authState.username, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary, letterSpacing = 0.5.sp)
                        Text(authState.email, fontSize = 13.sp, color = TextMuted)
                    }
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { authVm.logout() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Rose),
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Salir", fontSize = 12.sp)
                    }
                }
            }
        }

        // Stats grid
        item {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox("Reseñas", "${profileState.reviews.size}", Cyan, Modifier.weight(1f))
                StatBox("Jugando", "${profileState.playingCount}", Lime, Modifier.weight(1f))
                StatBox("Completados", "${profileState.completedCount}", Cyan, Modifier.weight(1f))
                StatBox("Pendientes", "${profileState.backlogCount}", TextMuted, Modifier.weight(1f))
            }
        }

        // Reviews section
        item {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.padding(horizontal = 12.dp)) {
                SectionHeader("Mis Reseñas")
            }
        }

        if (profileState.isLoading) {
            item { LoadingBox() }
        } else if (profileState.reviews.isEmpty()) {
            item { EmptyState("No has escrito reseñas aún.") }
        } else {
            items(profileState.reviews) { review ->
                MyReviewRow(review, onGameClick)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(CardBg)
            .border(1.dp, BorderColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.5.sp)
    }
}

@Composable
fun MyReviewRow(review: MyReviewItem, onGameClick: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(CardBg)
            .border(1.dp, BorderColor)
            .clickable { onGameClick(review.game.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(Surface2)
                .border(1.dp, BorderColor),
            contentAlignment = Alignment.Center
        ) { Text("🎮", fontSize = 16.sp) }
        Column(Modifier.weight(1f)) {
            Text(review.game.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, maxLines = 1)
            if (review.text.isNotBlank()) {
                Text(review.text.take(60) + if (review.text.length > 60) "…" else "", fontSize = 12.sp, color = TextMuted, maxLines = 1)
            } else {
                Text("Sin texto", fontSize = 12.sp, color = TextMuted)
            }
        }
        Text("●${review.rating}/10", color = Lime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
