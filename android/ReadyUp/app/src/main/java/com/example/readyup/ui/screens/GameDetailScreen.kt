package com.example.readyup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.readyup.ui.components.*
import com.example.readyup.ui.theme.*
import com.example.readyup.viewmodel.GamesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(vm: GamesViewModel, gameId: Int, onBack: () -> Unit) {
    val state by vm.detailState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var myRating by remember { mutableStateOf(0) }
    var myText by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Load game on enter
    LaunchedEffect(gameId) { vm.loadGameDetail(gameId) }

    // Populate my review fields when loaded
    LaunchedEffect(state.myReview) {
        state.myReview?.let { r ->
            myRating = r.rating
            myText = r.text
        }
    }

    // Show toasts
    LaunchedEffect(state.toastMsg) {
        state.toastMsg?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            vm.clearToast()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Box(
                    Modifier
                        .padding(16.dp)
                        .background(Surface)
                        .border(1.dp, Cyan)
                        .padding(12.dp, 10.dp)
                ) {
                    Text(data.visuals.message, color = TextPrimary, fontSize = 14.sp)
                }
            }
        },
        containerColor = BgDark,
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .border(width = 1.dp, color = BorderColor)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = { vm.clearDetail(); onBack() }) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Cyan)
                }
                Box(Modifier.align(Alignment.Center)) {
                    Text(
                        state.game?.title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingBox(Modifier.fillMaxSize().padding(padding))
            state.error != null -> EmptyState(state.error!!, Modifier.fillMaxSize().padding(padding))
            state.game == null -> EmptyState("Juego no encontrado", Modifier.fillMaxSize().padding(padding))
            else -> {
                val game = state.game!!
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Surface)
                            .border(1.dp, BorderColor)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Cover
                        Box(
                            Modifier
                                .width(100.dp)
                                .aspectRatio(3f / 4f)
                                .background(Surface2)
                        ) {
                            if (game.coverUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(game.coverUrl).crossfade(true).build(),
                                    contentDescription = game.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("🎮", fontSize = 32.sp)
                                }
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(game.title, style = MaterialTheme.typography.headlineMedium, fontSize = 20.sp)
                            Spacer(Modifier.height(8.dp))
                            // Tags
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                game.genres.take(2).forEach { g ->
                                    Box(Modifier.background(Surface2).border(1.dp, BorderColor).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                        Text(g, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.5.sp)
                                    }
                                }
                                game.releaseDate?.take(4)?.let { y ->
                                    Box(Modifier.background(Surface2).border(1.dp, BorderColor).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                        Text(y, fontSize = 10.sp, color = TextMuted)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            // Platforms
                            if (game.platforms.isNotEmpty()) {
                                Text(game.platforms.joinToString(" · "), fontSize = 11.sp, color = TextMuted)
                            }
                            Spacer(Modifier.height(12.dp))
                            // Status picker
                            StatusDropdown(
                                currentStatus = state.myStatus,
                                onStatusSelected = { vm.setStatus(gameId, it) }
                            )
                        }
                    }

                    // Description
                    if (!game.description.isNullOrBlank()) {
                        Column(Modifier.padding(16.dp)) {
                            SectionHeader("Descripción")
                            Text(game.description, style = MaterialTheme.typography.bodyLarge, color = TextMuted, lineHeight = 22.sp)
                        }
                    }

                    Divider(color = BorderColor)

                    // My review
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader(if (state.myReview != null) "Mi Reseña" else "Escribe una reseña")

                        // Rating
                        Text("Puntuación", fontSize = 10.sp, color = TextMuted, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        RatingPicker(myRating, { myRating = it })
                        Spacer(Modifier.height(12.dp))

                        // Text
                        OutlinedTextField(
                            value = myText,
                            onValueChange = { myText = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            placeholder = { Text("Escribe tu opinión...", color = TextMuted, fontSize = 14.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Cyan,
                                focusedContainerColor = BgDark,
                                unfocusedContainerColor = BgDark
                            ),
                            shape = RoundedCornerShape(0.dp)
                        )
                        if (state.reviewError != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(state.reviewError!!, color = Rose, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (myRating > 0) vm.submitReview(gameId, myRating, myText)
                                },
                                enabled = myRating > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Cyan,
                                    disabledContainerColor = Surface2
                                ),
                                shape = RoundedCornerShape(0.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    if (state.myReview != null) "Actualizar" else "Publicar",
                                    color = if (myRating > 0) Color.Black else TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            if (state.myReview != null) {
                                OutlinedButton(
                                    onClick = { showDeleteConfirm = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Rose),
                                    shape = RoundedCornerShape(0.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Eliminar", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Divider(color = BorderColor)

                    // Community reviews
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Reseñas (${state.reviews.size})")
                        if (state.reviews.isEmpty()) {
                            EmptyState("Sin reseñas aún. ¡Sé el primero!")
                        } else {
                            state.reviews.forEach { review ->
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BorderColor)
                                        .background(CardBg)
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        InitialsAvatar(review.user.username, 28)
                                        Spacer(Modifier.width(10.dp))
                                        Text(review.user.username, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                        Spacer(Modifier.weight(1f))
                                        Text("●${review.rating}/10", color = Lime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    if (review.text.isNotBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(review.text, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(formatDate(review.updatedAt), fontSize = 11.sp, color = TextMuted)
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirm dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar reseña", color = TextPrimary) },
            text = { Text("¿Seguro que quieres eliminar tu reseña?", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = { vm.deleteReview(gameId); showDeleteConfirm = false }) {
                    Text("Eliminar", color = Rose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(0.dp)
        )
    }
}

@Composable
fun StatusDropdown(currentStatus: String?, onStatusSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("PLAYING" to "Jugando", "BACKLOG" to "Pendiente", "COMPLETED" to "Completado", "DROPPED" to "Abandonado")
    val label = currentStatus?.let { s -> statuses.find { it.first == s }?.second } ?: "Añadir a biblioteca"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Cyan.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("◈  $label", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Surface).border(1.dp, BorderColor)
        ) {
            statuses.forEach { (val_, lbl) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            lbl,
                            color = if (currentStatus == val_) Cyan else TextPrimary,
                            fontWeight = if (currentStatus == val_) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onStatusSelected(val_); expanded = false }
                )
            }
        }
    }
}

fun formatDate(iso: String): String {
    return try {
        val parts = iso.take(10).split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (e: Exception) { iso.take(10) }
}
