package com.example.readyup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.readyup.data.remote.Api
import com.example.readyup.ui.theme.ReadyUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadyUpTheme {
                GameSmokeTest()
            }
        }
    }
}

@Composable
fun GameSmokeTest() {
    var text by remember { mutableStateOf("Cargando /games...") }

    LaunchedEffect(Unit) {
        text = try {
            val res = Api.service.getGames(limit = 1, offset = 0)
            val first = res.results.firstOrNull()?.title ?: "Sin resultados"
            "OK. count=${res.count}. First=$first"
        } catch (e: Exception) {
            "Error llamando /games: ${e.message}"
        }
    }

    Text(text = text)

}