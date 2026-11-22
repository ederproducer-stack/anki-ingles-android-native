package com.ederproducer.ankiingles.mobile.model

import kotlin.math.max
import kotlin.math.roundToInt

/** Repetição espaçada: SM-2 simplificado, portado do srs.js do webapp. */
object Srs {
    const val DAY = 24L * 60 * 60 * 1000
    const val MINUTE = 60L * 1000
    private const val MIN_EF = 1.3
    private const val MAX_EF = 3.0

    private fun clamp(value: Double, lo: Double, hi: Double) = value.coerceIn(lo, hi)

    fun newCard(id: String, front: String, back: String, notes: String = ""): Card {
        val now = System.currentTimeMillis()
        return Card(
            id = id, front = front, back = back, notes = notes,
            due = now, created = now
        )
    }

    fun schedule(card: Card, rating: Rating, now: Long = System.currentTimeMillis()): ScheduleResult {
        val c = card.copy()
        c.lastReviewed = now
        var requeue = false

        when (rating) {
            Rating.AGAIN -> {
                c.lapses += 1
                c.reps = 0
                c.intervalDays = 0
                c.ef = clamp(c.ef - 0.20, MIN_EF, 2.5)
                c.due = now + MINUTE
                requeue = true
            }
            Rating.HARD -> {
                c.intervalDays = max(1, ((if (c.intervalDays > 0) c.intervalDays else 1) * 1.2).roundToInt())
                c.ef = clamp(c.ef - 0.15, MIN_EF, 2.5)
                c.reps += 1
                c.due = now + c.intervalDays * DAY
            }
            Rating.GOOD -> {
                c.intervalDays = when (c.reps) {
                    0 -> 1
                    1 -> 6
                    else -> max(1, (c.intervalDays * c.ef).roundToInt())
                }
                c.reps += 1
                c.due = now + c.intervalDays * DAY
            }
            Rating.EASY -> {
                c.intervalDays = when (c.reps) {
                    0 -> 4
                    1 -> (6 * 1.3).roundToInt()
                    else -> max(1, (c.intervalDays * c.ef * 1.3).roundToInt())
                }
                c.ef = clamp(c.ef + 0.15, MIN_EF, MAX_EF)
                c.reps += 1
                c.due = now + c.intervalDays * DAY
            }
        }
        return ScheduleResult(c, requeue)
    }

    fun formatInterval(card: Card, rating: Rating, now: Long = System.currentTimeMillis()): String {
        if (rating == Rating.AGAIN) return "1 min"
        val days = schedule(card, rating, now).card.intervalDays
        return when {
            days <= 1 -> "1 dia"
            days < 30 -> "$days dias"
            else -> {
                val months = (days / 30.0).roundToInt()
                if (months <= 1) "1 mês" else "$months meses"
            }
        }
    }
}
