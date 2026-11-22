package com.ederproducer.ankiingles.mobile.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ederproducer.ankiingles.mobile.data.BackupRepository
import com.ederproducer.ankiingles.mobile.model.Card
import com.ederproducer.ankiingles.mobile.model.DailyStats
import com.ederproducer.ankiingles.mobile.model.DeckState
import com.ederproducer.ankiingles.mobile.model.Rating
import com.ederproducer.ankiingles.mobile.model.Srs
import java.time.LocalDate

/** Estado único do app: cartões, sessão de estudo e persistência. */
class AppState(context: Context) {
    private val appContext = context.applicationContext
    var deck: DeckState by mutableStateOf(BackupRepository.load(appContext))
        private set

    val queue = mutableStateListOf<Card>()
    var current: Card? by mutableStateOf(null)
        private set
    var flipped: Boolean by mutableStateOf(false)
        private set
    var reviewedToday: Int by mutableStateOf(0)
        private set
    var freeMode: Boolean by mutableStateOf(false)
        private set
    var settingsVersion: Int by mutableStateOf(0)
        private set

    init {
        startSession()
    }

    fun startSession(free: Boolean = freeMode) {
        freeMode = free
        val now = System.currentTimeMillis()
        val due = deck.cards
            .filter { free || it.due <= now }
            .sortedBy { it.due }
            .take(deck.settings.dailyLimit)
        queue.clear()
        queue.addAll(due)
        current = queue.firstOrNull()
        flipped = false
    }

    fun flip() { flipped = !flipped }

    fun rate(rating: Rating) {
        val card = current ?: return
        val now = System.currentTimeMillis()
        val result = Srs.schedule(card, rating, now)
        val index = deck.cards.indexOfFirst { it.id == card.id }
        if (index >= 0) deck.cards[index] = result.card
        if (result.requeue) queue.add(result.card)
        queue.remove(card)
        current = queue.firstOrNull()
        flipped = false
        reviewedToday += 1
        recordHistory(rating)
        persist()
    }

    private fun recordHistory(rating: Rating) {
        val day = LocalDate.now().toString()
        val h = deck.history.getOrPut(day) { DailyStats() }
        when (rating) {
            Rating.AGAIN -> h.again += 1
            Rating.HARD -> h.hard += 1
            Rating.GOOD -> h.good += 1
            Rating.EASY -> h.easy += 1
        }
    }

    fun addCard(front: String, back: String, notes: String = "") {
        deck.cards.add(Srs.newCard(java.util.UUID.randomUUID().toString(), front.trim(), back.trim(), notes.trim()))
        persist()
    }

    fun updateCard(id: String, front: String, back: String, notes: String) {
        val index = deck.cards.indexOfFirst { it.id == id }
        if (index < 0) return
        val old = deck.cards[index]
        deck.cards[index] = old.copy(front = front.trim(), back = back.trim(), notes = notes.trim())
        persist()
    }

    fun deleteCard(id: String) {
        deck.cards.removeAll { it.id == id }
        persist()
    }

    fun importCards(cards: List<Card>) {
        deck.cards.addAll(cards)
        persist()
    }

    fun updateSettings(block: (com.ederproducer.ankiingles.mobile.model.Settings) -> Unit) {
        block(deck.settings)
        settingsVersion += 1
        persist()
    }

    fun persist() {
        if (deck.settings.autoBackup) BackupRepository.save(appContext, deck)
    }

    fun saveNow() = BackupRepository.save(appContext, deck)

    fun restore(text: String): Boolean {
        val parsed = BackupRepository.parseBackup(text) ?: return false
        deck = parsed
        startSession()
        return BackupRepository.save(appContext, deck)
    }

    fun resetToSeed() {
        deck.cards.clear()
        deck.cards.addAll(com.ederproducer.ankiingles.mobile.model.SeedDeck.create())
        deck.history.clear()
        startSession()
        persist()
    }
}
