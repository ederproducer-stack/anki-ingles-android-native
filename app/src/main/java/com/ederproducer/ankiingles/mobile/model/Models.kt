package com.ederproducer.ankiingles.mobile.model

/** Cartão de estudo. Os campos de SRS são idênticos aos do backup da versão web. */
data class Card(
    val id: String,
    var front: String,
    var back: String,
    var notes: String = "",
    var reps: Int = 0,
    var intervalDays: Int = 0,
    var ef: Double = 2.5,
    var due: Long = System.currentTimeMillis(),
    var lapses: Int = 0,
    var created: Long = System.currentTimeMillis(),
    var lastReviewed: Long? = null
)

data class DailyStats(
    var again: Int = 0,
    var hard: Int = 0,
    var good: Int = 0,
    var easy: Int = 0
)

data class Settings(
    var autoplay: Boolean = true,
    var rate: Float = 1f,
    var dailyLimit: Int = 30,
    var newPerDay: Int = 10,
    var autoBackup: Boolean = true
)

class DeckState(
    val cards: MutableList<Card> = mutableListOf(),
    var settings: Settings = Settings(),
    val history: MutableMap<String, DailyStats> = linkedMapOf()
)

enum class Rating { AGAIN, HARD, GOOD, EASY }

data class ScheduleResult(val card: Card, val requeue: Boolean)
