package com.ederproducer.ankiingles.mobile.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ederproducer.ankiingles.mobile.data.BackupRepository

@Composable
fun SettingsScreen(state: AppState, ttsAvailable: Boolean) {
    val context = LocalContext.current
    var restoreOpen by remember { mutableStateOf(false) }
    // Lê a versão para recompor quando as configurações mudam.
    state.settingsVersion

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Pasta de backup", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Definida pelo app, sem seletor de pasta. Cartões e configurações em JSON; nenhum áudio é salvo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        BackupRepository.displayPath(context),
                        style = MaterialTheme.typography.bodySmall,
                        color = Accent,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            val ok = state.saveNow()
                            Toast.makeText(context, if (ok) "Backup salvo." else "Falha ao salvar.", Toast.LENGTH_SHORT).show()
                        }) { Text("Salvar agora") }
                        OutlinedButton(onClick = { restoreOpen = true }) { Text("Restaurar") }
                    }
                }
            }
        }

        item {
            SettingSwitch(
                title = "Salvar backup automaticamente",
                subtitle = "Atualiza o JSON após cada alteração.",
                checked = state.deck.settings.autoBackup,
                onCheckedChange = { value -> state.updateSettings { it.autoBackup = value } }
            )
        }

        item {
            SettingSwitch(
                title = "Voz automática",
                subtitle = "Fala a frase em inglês ao mostrá-la.",
                checked = state.deck.settings.autoplay,
                onCheckedChange = { value -> state.updateSettings { it.autoplay = value } }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Velocidade da voz", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.75f, 1f, 1.25f).forEach { rate ->
                            val selected = state.deck.settings.rate == rate
                            Button(
                                onClick = { state.updateSettings { it.rate = rate } },
                                modifier = Modifier.weight(1f),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (selected) Accent else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) Background else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    "${rate.toString().replace('.', ',')}×",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Voz em inglês", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (ttsAvailable)
                            "Usando a voz do Android em en-US. Sem áudios salvos e sem depender da internet."
                        else
                            "Nenhuma voz em inglês disponível. Instale a voz em inglês nas configurações de TTS do Android.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (ttsAvailable) MaterialTheme.colorScheme.onSurfaceVariant else Again,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (!ttsAvailable) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                                }.onFailure {
                                    Toast.makeText(context, "Abra Ajustes → Acessibilidade → Síntese de voz.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.padding(top = 10.dp)
                        ) { Text("Configurar voz") }
                    }
                }
            }
        }

        item {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Criado por Eder Oliveira", style = MaterialTheme.typography.bodySmall)
                Text(
                    "ederproducer@gmail.com",
                    style = MaterialTheme.typography.bodySmall.copy(color = Accent, textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:ederproducer@gmail.com")))
                    }
                )
                Text(
                    "código no GitHub",
                    style = MaterialTheme.typography.bodySmall.copy(color = Accent, textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ederproducer-stack/anki-ingles-android-native")))
                    }
                )
            }
        }
    }

    if (restoreOpen) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { restoreOpen = false },
            title = { Text("Restaurar backup") },
            text = {
                OutlinedTextField(
                    text, { text = it },
                    label = { Text("Cole o JSON do backup") },
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val ok = state.restore(text)
                    Toast.makeText(context, if (ok) "Backup restaurado." else "JSON inválido.", Toast.LENGTH_SHORT).show()
                    restoreOpen = false
                }) { Text("Restaurar") }
            },
            dismissButton = { TextButton(onClick = { restoreOpen = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
