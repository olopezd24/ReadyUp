package com.example.readyup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readyup.data.model.FollowUser
import com.example.readyup.ui.components.*
import com.example.readyup.ui.theme.*
import com.example.readyup.viewmodel.AuthViewModel
import com.example.readyup.viewmodel.UsersViewModel
import kotlinx.coroutines.launch

// ── Búsqueda de usuarios ──────────────────────────────────────────────────────

@Composable
fun UsersScreen(
    vm: UsersViewModel,
    onUserClick: (Int) -> Unit
) {
    val state by vm.searchState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(BgDark)) {
        // Search bar
        Row(
            Modifier
                .fillMaxWidth()
                .background(Surface)
                .border(1.dp, BorderColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.weight(1f).height(48.dp),
                placeholder = { Text("Buscar usuario...", color = TextMuted, fontSize = 14.sp) },
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
                onClick = { vm.searchUsers(searchText) },
                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.height(48.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Buscar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        when {
            state.isLoading -> LoadingBox(Modifier.fillMaxSize())
            state.error != null -> EmptyState(state.error!!, Modifier.fillMaxSize())
            state.query.isBlank() -> EmptyState("Busca un usuario por su nombre", Modifier.fillMaxSize())
            state.results.isEmpty() -> EmptyState("No se encontraron usuarios", Modifier.fillMaxSize())
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "${state.results.size} usuario${if (state.results.size != 1) "s" else ""} encontrado${if (state.results.size != 1) "s" else ""}",
                            fontSize = 11.sp, color = TextMuted, letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(state.results) { user ->
                        UserRow(user = user, onClick = { onUserClick(user.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun UserRow(user: FollowUser, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardBg)
            .border(1.dp, BorderColor)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InitialsAvatar(user.username, 40)
        Text(
            user.username,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text("→", color = Cyan, fontSize = 16.sp)
    }
}

// ── Perfil de otro usuario ────────────────────────────────────────────────────

@Composable
fun UserDetailScreen(
    vm: UsersViewModel,
    authVm: AuthViewModel,
    userId: Int,
    onBack: () -> Unit,
    onGameClick: (Int) -> Unit
) {
    val state by vm.detailState.collectAsState()
    val authState by authVm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        vm.loadUserDetail(userId, authState.userId)
    }

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
                    Modifier.padding(16.dp).background(Surface).border(1.dp, Cyan).padding(12.dp, 10.dp)
                ) {
                    Text(data.visuals.message, color = TextPrimary, fontSize = 14.sp)
                }
            }
        },
        containerColor = BgDark,
        topBar = {
            Box(
                Modifier.fillMaxWidth().background(Surface).border(1.dp, BorderColor).padding(4.dp)
            ) {
                IconButton(onClick = { vm.clearDetail(); onBack() }) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Cyan)
                }
                Box(Modifier.align(Alignment.Center)) {
                    Text(
                        state.user?.username ?: "Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingBox(Modifier.fillMaxSize().padding(padding))
            state.error != null -> EmptyState(state.error!!, Modifier.fillMaxSize().padding(padding))
            else -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Surface)
                            .border(1.dp, BorderColor)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val username = state.user?.username ?: "Usuario"
                        InitialsAvatar(username, 64)
                        Spacer(Modifier.height(12.dp))
                        Text(username, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
                        Spacer(Modifier.height(16.dp))

                        // Estadísticas
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Reseñas", "${state.reviews.size}")
                            StatItem("Seguidores", "${state.followers.size}")
                            StatItem("Siguiendo", "${state.following.size}")
                        }

                        Spacer(Modifier.height(16.dp))

                        // Botón seguir — no mostrar si es el propio usuario
                        if (userId != authState.userId) {
                            Button(
                                onClick = { vm.toggleFollow(userId, authState.userId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isFollowing) Surface2 else Cyan
                                ),
                                shape = RoundedCornerShape(0.dp),
                                border = if (state.isFollowing)
                                    androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                                else null,
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text(
                                    if (state.isFollowing) "Dejar de seguir" else "Seguir",
                                    color = if (state.isFollowing) TextMuted else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Reseñas del usuario
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Reseñas de ${state.user?.username ?: "este usuario"}")
                        if (state.reviews.isEmpty()) {
                            EmptyState("Este usuario no tiene reseñas aún")
                        } else {
                            state.reviews.forEach { review ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(CardBg)
                                        .border(1.dp, BorderColor)
                                        .clickable { onGameClick(review.game.id) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        Modifier.size(36.dp).background(Surface2).border(1.dp, BorderColor),
                                        contentAlignment = Alignment.Center
                                    ) { Text("🎮", fontSize = 16.sp) }
                                    Column(Modifier.weight(1f)) {
                                        Text(review.game.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, maxLines = 1)
                                        if (review.text.isNotBlank()) {
                                            Text(review.text.take(60) + if (review.text.length > 60) "…" else "", fontSize = 12.sp, color = TextMuted, maxLines = 1)
                                        }
                                    }
                                    Text("●${review.rating}/10", color = Lime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Cyan)
        Text(label, fontSize = 11.sp, color = TextMuted, letterSpacing = 0.5.sp)
    }
}