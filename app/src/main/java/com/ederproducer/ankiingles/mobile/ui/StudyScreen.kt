package com.ederproducer.ankiingles.mobile.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ederproducer.ankiingles.mobile.model.Rating
import com.ederproducer.ankiingles.mobile.model.Srs
import com.ederproducer.ankiingles.mobile.tts.EnglishTts

@Composable
fun StudyScreen(
    state: AppState,
    tts: EnglishTts,
    ttsAvailable: Boolean,
    onAddCard: () -> Unit,
    onVoiceError: () -> Unit
) {
    val card = state.current

    LaunchedEffect(card?.id, state.flipped, ttsAvailable) {
        if (ttsAvailable && card != null && !state.flipped && state.deck.settings.autoplay) {
            tts.speak(RichText.plain(card.front), state.deck.settings.rate) { ok ->
                if (!ok) onVoiceError()
            }
        }
    }

    if (card == null) {
        EmptyStudyState(state, onAddCard)
        return
    }

    val totalDue = state.deck.cards.count { it.due <= System.currentTimeMillis() }
    val remaining = state.queue.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sessão de revisão", style = MaterialTheme.typography.titleLarge)
                Text(
                    "$remaining restantes • $totalDue disponíveis no total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LinearProgressIndicator(
            progress = { if (state.deck.cards.isEmpty()) 0f else 1f - remaining.toFloat() / state.deck.cards.size },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = Accent,
            trackColor = SurfaceDark
        )

        FlipCard(
            card = card,
            flipped = state.flipped,
            onSpeak = {
                if (!ttsAvailable) onVoiceError()
                else tts.speak(RichText.plain(card.front), state.deck.settings.rate) { ok ->
                    if (!ok) onVoiceError()
                }
            }
        )

        if (!state.flipped) {
            Button(
                onClick = { state.flip() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Background)
            ) {
                Text("Mostrar resposta", fontSize = 17.sp)
            }
        } else {
            RatingRow(card, state)
        }
    }
}

@Composable
private fun FlipCard(card: com.ederproducer.ankiingles.mobile.model.Card, flipped: Boolean, onSpeak: () -> Unit) {
    var target by remember(card.id) { mutableFloatStateOf(0f) }
    target = if (flipped) 180f else 0f
    val rotation by animateFloatAsState(targetValue = target, animationSpec = spring(stiffness = 320f), label = "flip")
    val showFront = rotation < 90f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (flipped) 240.dp else 280.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(Modifier.fillMaxSize().padding(22.dp)) {
            if (showFront) {
                CardFace(
                    label = "🇺🇸 INGLÊS",
                    text = RichText.annotated(card.front),
                    buttonText = "🔊  Ouvir frase",
                    onSpeak = onSpeak
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                ) {
                    CardFace(
                        label = "🇧🇷 PORTUGUÊS",
                        text = RichText.annotated(card.back),
                        buttonText = "🔊  Ouvir em inglês",
                        onSpeak = onSpeak
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFace(
    label: String,
    text: androidx.compose.ui.text.AnnotatedString,
    buttonText: String,
    onSpeak: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, lineHeight = 32.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSpeak) { Text(buttonText) }
        Spacer(Modifier.height(14.dp))
        Text(
            if (label.contains("INGLÊS")) "Clique em “Mostrar resposta” para virar" else "Avalie sua lembrança abaixo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RatingRow(card: com.ederproducer.ankiingles.mobile.model.Card, state: AppState) {
    val ratings = listOf(
        Rating.AGAIN to Again,
        Rating.HARD to Hard,
        Rating.GOOD to Good,
        Rating.EASY to Easy
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ratings.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (rating, color) ->
                    Button(
                        onClick = { state.rate(rating) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = color.copy(alpha = 0.16f),
                            contentColor = color
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                when (rating) {
                                    Rating.AGAIN -> "De novo"
                                    Rating.HARD -> "Difícil"
                                    Rating.GOOD -> "Bom"
                                    Rating.EASY -> "Fácil"
                                },
                                fontSize = 14.sp
                            )
                            Text(Srs.formatInterval(card, rating), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStudyState(state: AppState, onAddCard: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text("Tudo em dia por aqui!", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Você não tem cartões para revisar agora.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = { state.startSession(free = true) }) {
            Text("Praticar todos")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onAddCard) { Text("Adicionar cartão") }
    }
}
