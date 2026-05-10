package com.example.readyup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readyup.ui.components.*
import com.example.readyup.ui.theme.*
import com.example.readyup.viewmodel.GamesViewModel

@Composable
fun GamesScreen(vm: GamesViewModel, onGameClick: (Int) -> Unit) {
    val state by vm.gamesState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (state.games.isEmpty()) vm.loadGames()
    }

    Column(Modifier.fillMaxSize().background(BgDark)) {
        // Search bar
        Row(
            Modifier
                .fillMaxWidth()
                .background(Surface)
                .border(width = 1.dp, color = BorderColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it; vm.setQuery(it) },
                modifier = Modifier.weight(1f).height(48.dp),
                placeholder = { Text("Buscar juego...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                singleLine = true,
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
            Button(
                onClick = { vm.loadGames(0) },
                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.height(48.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Buscar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Sort filters
        Row(
            Modifier
                .fillMaxWidth()
                .background(Surface2)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ordenar:", color = TextMuted, fontSize = 11.sp, letterSpacing = 0.5.sp)
            listOf("" to "Todos", "new" to "Nuevos", "top" to "Top", "popular" to "Popular").forEach { (val_, label) ->
                FilterChip(val_, label, state.sort == val_) { vm.setSort(val_) }
            }
            Spacer(Modifier.weight(1f))
            if (state.query.isNotBlank() || state.sort.isNotBlank()) {
                TextButton(
                    onClick = { searchText = ""; vm.clearFilters() },
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("Limpiar", color = Rose, fontSize = 11.sp)
                }
            }
        }

        // Games grid
        when {
            state.isLoading -> LoadingBox(Modifier.fillMaxSize())
            state.error != null -> EmptyState(state.error!!, Modifier.fillMaxSize())
            state.games.isEmpty() -> EmptyState("No se encontraron juegos", Modifier.fillMaxSize())
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.games) { game ->
                        GameCard(
                            title = game.title,
                            coverUrl = game.coverUrl,
                            releaseYear = game.releaseDate?.take(4),
                            rating = game.avgRating,
                            onClick = { onGameClick(game.id) }
                        )
                    }

                    // Pagination footer
                    if (state.count > state.games.size + state.offset) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { vm.loadGames(state.offset + GamesViewModel.PAGE_SIZE) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Surface2),
                                    shape = RoundedCornerShape(0.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                                ) {
                                    Text("Cargar más", color = TextPrimary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(value: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .border(1.dp, if (isSelected) Cyan else BorderColor)
            .background(if (isSelected) Cyan.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = if (isSelected) Cyan else TextMuted, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
