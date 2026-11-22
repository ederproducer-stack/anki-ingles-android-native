package com.ederproducer.ankiingles.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ederproducer.ankiingles.mobile.data.BackupRepository
import com.ederproducer.ankiingles.mobile.model.Card

@Composable
fun CardsScreen(state: AppState, onSpeak: (String) -> Unit) {
    var search by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Card?>(null) }
    var importing by remember { mutableStateOf(false) }

    val list = state.deck.cards.filter {
        search.isBlank() ||
            RichText.plain(it.front).contains(search, true) ||
            RichText.plain(it.back).contains(search, true)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Seus cartões", style = MaterialTheme.typography.titleLarge)
                Text(
                    "${state.deck.cards.size} cartões",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { importing = true }) {
                    Icon(Icons.Default.Download, contentDescription = "Importar", tint = Accent)
                }
                IconButton(onClick = { editing = Card(id = "", front = "", back = "") }) {
                    Icon(Icons.Default.Add, contentDescription = "Novo cartão", tint = Accent)
                }
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Buscar frase...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(list, key = { it.id }) { card ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f).clickable { onSpeak(RichText.plain(card.front)) }) {
                            Text(
                                RichText.plain(card.front),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                RichText.plain(card.back),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { editing = card.copy() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Accent)
                        }
                        IconButton(onClick = { state.deleteCard(card.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Again)
                        }
                    }
                }
            }
        }
    }

    editing?.let { card ->
        CardEditor(
            card = card,
            onSave = { front, back, notes ->
                if (card.id.isEmpty()) state.addCard(front, back, notes)
                else state.updateCard(card.id, front, back, notes)
                editing = null
            },
            onCancel = { editing = null }
        )
    }

    if (importing) {
        ImportDialog(
            onImport = { text ->
                val cards = BackupRepository.parseImport(text)
                if (cards.isNotEmpty()) state.importCards(cards)
                importing = false
            },
            onCancel = { importing = false }
        )
    }
}

@Composable
private fun CardEditor(
    card: Card,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var front by remember { mutableStateOf(RichText.plain(card.front)) }
    var back by remember { mutableStateOf(RichText.plain(card.back)) }
    var notes by remember { mutableStateOf(card.notes) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (card.id.isEmpty()) "Novo cartão" else "Editar cartão") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(front, { front = it }, label = { Text("Inglês") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(back, { back = it }, label = { Text("Português") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notes, { notes = it }, label = { Text("Notas (opcional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { if (front.isNotBlank() && back.isNotBlank()) onSave(front, back, notes) }) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@Composable
private fun ImportDialog(onImport: (String) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Importar cartões") },
        text = {
            Column {
                Text(
                    "Cole linhas no formato inglês :: português ou um backup JSON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    text, { text = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text("Cartões") },
                    minLines = 5
                )
            }
        },
        confirmButton = { TextButton(onClick = { onImport(text) }) { Text("Importar") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}
