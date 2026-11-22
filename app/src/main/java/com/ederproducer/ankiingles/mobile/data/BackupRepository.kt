package com.ederproducer.ankiingles.mobile.data

import android.content.Context
import com.ederproducer.ankiingles.mobile.model.Card
import com.ederproducer.ankiingles.mobile.model.DailyStats
import com.ederproducer.ankiingles.mobile.model.DeckState
import com.ederproducer.ankiingles.mobile.model.SeedDeck
import com.ederproducer.ankiingles.mobile.model.Settings
import com.ederproducer.ankiingles.mobile.model.Srs
import org.json.JSONObject
import java.io.File

/**
 * Backup em pasta própria do app, no mesmo formato JSON do webapp.
 * Nenhum áudio é gravado: a voz é sempre falada pelo mecanismo do Android.
 */
object BackupRepository {
    private const val FILE_NAME = "anki-ingles-backup.json"

    fun directory(context: Context): File = File(context.getExternalFilesDir(null), "Anki")

    fun backupFile(context: Context): File = File(directory(context), FILE_NAME)

    fun displayPath(context: Context): String = backupFile(context).absolutePath

    fun load(context: Context): DeckState {
        val file = backupFile(context)
        if (!file.exists()) {
            val state = DeckState(SeedDeck.create())
            save(context, state)
            return state
        }
        return try {
            parseBackup(file.readText()) ?: DeckState(SeedDeck.create())
        } catch (e: Exception) {
            DeckState(SeedDeck.create())
        }
    }

    fun toJson(state: DeckState): String {
        val root = JSONObject()
        root.put("app", "anki-ingles-mobile")
        root.put("version", 2)
        root.put("exportedAt", java.time.Instant.now().toString())

        val cards = org.json.JSONArray()
        state.cards.forEach { c ->
            val o = JSONObject()
            o.put("id", c.id)
            o.put("front", c.front)
            o.put("back", c.back)
            o.put("notes", c.notes)
            o.put("reps", c.reps)
            o.put("interval", c.intervalDays)
            o.put("ef", c.ef)
            o.put("due", c.due)
            o.put("lapses", c.lapses)
            o.put("created", c.created)
            if (c.lastReviewed != null) o.put("lastReviewed", c.lastReviewed) else o.put("lastReviewed", JSONObject.NULL)
            cards.put(o)
        }
        root.put("cards", cards)

        val s = state.settings
        val settings = JSONObject()
        settings.put("autoplay", s.autoplay)
        settings.put("rate", s.rate.toDouble())
        settings.put("dailyLimit", s.dailyLimit)
        settings.put("newPerDay", s.newPerDay)
        settings.put("free", false)
        settings.put("autoBackup", s.autoBackup)
        root.put("settings", settings)

        val stats = JSONObject()
        state.history.forEach { (day, h) ->
            val o = JSONObject()
            o.put("again", h.again); o.put("hard", h.hard)
            o.put("good", h.good); o.put("easy", h.easy)
            stats.put(day, o)
        }
        root.put("stats", stats)
        return root.toString(2)
    }

    fun parseBackup(text: String): DeckState? {
        val root = JSONObject(text)
        val array = root.optJSONArray("cards") ?: return null
        val cards = mutableListOf<Card>()
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            val front = o.optString("front", "")
            val back = o.optString("back", "")
            if (front.isBlank() || back.isBlank()) continue
            cards.add(
                Card(
                    id = o.optString("id", java.util.UUID.randomUUID().toString()),
                    front = front,
                    back = back,
                    notes = o.optString("notes", ""),
                    reps = o.optInt("reps", 0),
                    intervalDays = o.optInt("interval", o.optInt("intervalDays", 0)),
                    ef = o.optDouble("ef", 2.5).takeIf { !it.isNaN() } ?: 2.5,
                    due = o.optLong("due", System.currentTimeMillis()),
                    lapses = o.optInt("lapses", 0),
                    created = o.optLong("created", System.currentTimeMillis()),
                    lastReviewed = if (o.isNull("lastReviewed")) null else o.optLong("lastReviewed", 0L)
                )
            )
        }
        if (cards.isEmpty()) return null

        val so = root.optJSONObject("settings")
        val settings = if (so == null) Settings() else Settings(
            autoplay = so.optBoolean("autoplay", true),
            rate = so.optDouble("rate", 1.0).toFloat(),
            dailyLimit = so.optInt("dailyLimit", 30),
            newPerDay = so.optInt("newPerDay", 10),
            autoBackup = so.optBoolean("autoBackup", true)
        )

        val history = linkedMapOf<String, DailyStats>()
        val stats = root.optJSONObject("stats")
        if (stats != null) {
            stats.keys().forEach { day ->
                val h = stats.getJSONObject(day)
                history[day] = DailyStats(
                    h.optInt("again"), h.optInt("hard"), h.optInt("good"), h.optInt("easy")
                )
            }
        }
        return DeckState(cards, settings, history)
    }

    /** Aceita um backup JSON ou linhas "inglês :: português". */
    fun parseImport(text: String): List<Card> {
        val trimmed = text.trim()
        if (trimmed.startsWith("{")) {
            return parseBackup(trimmed)?.cards?.toList() ?: emptyList()
        }
        return trimmed.lineSequence()
            .mapNotNull { line ->
                val parts = line.split(" :: ", "\t", limit = 2)
                if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                    SrsCard(parts[0].trim(), parts[1].trim())
                } else null
            }
            .map { (front, back) -> Srs.newCard(java.util.UUID.randomUUID().toString(), front, back) }
            .toList()
    }

    private data class SrsCard(val front: String, val back: String)

    fun save(context: Context, state: DeckState): Boolean = try {
        val dir = directory(context)
        if (!dir.exists()) dir.mkdirs()
        val target = backupFile(context)
        val temp = File(dir, "$FILE_NAME.tmp")
        temp.writeText(toJson(state))
        if (target.exists()) target.delete()
        temp.renameTo(target)
    } catch (e: Exception) {
        false
    }
}
