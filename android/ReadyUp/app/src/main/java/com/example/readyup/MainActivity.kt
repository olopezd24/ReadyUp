package com.example.readyup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readyup.ui.screens.*
import com.example.readyup.ui.theme.*
import com.example.readyup.viewmodel.AuthViewModel
import com.example.readyup.viewmodel.GamesViewModel
import com.example.readyup.viewmodel.LibraryViewModel

sealed class Screen(val route: String, val label: String, val icon: String) {
    object Games : Screen("games", "Juegos", "🎮")
    object Library : Screen("library", "Biblioteca", "📚")
    object Feed : Screen("feed", "Feed", "📡")
    object Profile : Screen("profile", "Perfil", "👤")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadyUpTheme {
                ReadyUpApp()
            }
        }
    }
}

@Composable
fun ReadyUpApp() {
    val authVm: AuthViewModel = viewModel()
    val gamesVm: GamesViewModel = viewModel()
    val libraryVm: LibraryViewModel = viewModel()
    val authState by authVm.state.collectAsState()

    if (!authState.isLoggedIn) {
        AuthScreen(vm = authVm)
    } else {
        MainHost(authVm, gamesVm, libraryVm)
    }
}

@Composable
fun MainHost(
    authVm: AuthViewModel,
    gamesVm: GamesViewModel,
    libraryVm: LibraryViewModel
) {
    val screens = listOf(Screen.Games, Screen.Library, Screen.Feed, Screen.Profile)
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Games) }
    var selectedGameId by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        Box(
            Modifier
                .fillMaxWidth()
                .background(Surface)
                .border(width = 1.dp, color = BorderColor)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopStart)
                    .background(Brush.horizontalGradient(listOf(Cyan, Rose)))
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("READY", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Cyan, letterSpacing = 2.sp)
                Text("UP", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Rose, letterSpacing = 2.sp)
            }
        }

        // Content
        Box(Modifier.weight(1f)) {
            if (selectedGameId != null) {
                GameDetailScreen(
                    vm = gamesVm,
                    gameId = selectedGameId!!,
                    onBack = { selectedGameId = null }
                )
            } else {
                when (currentScreen) {
                    Screen.Games -> GamesScreen(gamesVm) { selectedGameId = it }
                    Screen.Library -> LibraryScreen(libraryVm) { selectedGameId = it }
                    Screen.Feed -> FeedScreen(libraryVm) { selectedGameId = it }
                    Screen.Profile -> ProfileScreen(authVm, libraryVm) { selectedGameId = it }
                }
            }
        }

        // Bottom nav
        AnimatedVisibility(visible = selectedGameId == null, enter = fadeIn(), exit = fadeOut()) {
            BottomNavBar(screens, currentScreen) {
                currentScreen = it
                selectedGameId = null
            }
        }
    }
}

@Composable
fun BottomNavBar(screens: List<Screen>, current: Screen, onSelect: (Screen) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Surface)
            .border(1.dp, BorderColor)
            .navigationBarsPadding()
            .height(58.dp)
    ) {
        screens.forEach { screen ->
            val isActive = screen == current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(screen) }
                    .background(if (isActive) Surface2 else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isActive) {
                        Box(Modifier.fillMaxWidth().height(2.dp).background(Cyan))
                    }
                    Spacer(Modifier.height(if (isActive) 6.dp else 8.dp))
                    Text(screen.icon, fontSize = 18.sp)
                    Text(
                        screen.label,
                        fontSize = 10.sp,
                        color = if (isActive) Cyan else TextMuted,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}
