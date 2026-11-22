package com.ederproducer.ankiingles.mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ederproducer.ankiingles.mobile.tts.EnglishTts
import com.ederproducer.ankiingles.mobile.ui.Accent
import com.ederproducer.ankiingles.mobile.ui.AppState
import com.ederproducer.ankiingles.mobile.ui.CardsScreen
import com.ederproducer.ankiingles.mobile.ui.RichText
import com.ederproducer.ankiingles.mobile.ui.SettingsScreen
import com.ederproducer.ankiingles.mobile.ui.StudyScreen
import com.ederproducer.ankiingles.mobile.ui.AnkiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnkiTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val state = remember { AppState(context) }
    var ttsAvailable by remember { mutableStateOf(false) }
    val tts = remember { EnglishTts(context) { ttsAvailable = it } }

    DisposableEffect(tts) {
        onDispose { tts.shutdown() }
    }

    var screen by remember { mutableStateOf("study") }
    val voiceError = {
        Toast.makeText(context, "Não foi possível falar. Verifique a voz em Ajustes.", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val items = listOf(
                    Triple("study", "Revisar", Icons.Default.Home),
                    Triple("cards", "Cartões", Icons.Default.List),
                    Triple("settings", "Ajustes", Icons.Default.Settings)
                )
                items.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = screen == route,
                        onClick = { screen = route },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent,
                            selectedTextColor = Accent,
                            indicatorColor = Accent.copy(alpha = 0.16f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (screen) {
                "cards" -> CardsScreen(state, onSpeak = { text ->
                    if (!ttsAvailable) voiceError()
                    else tts.speak(RichText.plain(text), state.deck.settings.rate) { ok -> if (!ok) voiceError() }
                })
                "settings" -> SettingsScreen(state, ttsAvailable)
                else -> StudyScreen(
                    state = state,
                    tts = tts,
                    ttsAvailable = ttsAvailable,
                    onAddCard = { screen = "cards" },
                    onVoiceError = voiceError
                )
            }
        }
    }
}
