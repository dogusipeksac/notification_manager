package com.dogusipeksac.notification_programming.domain

import java.util.Calendar

/**
 * Sessiz saat aralığının gün içinde / gece yarısını saran durumlarda doğru hesaplanması.
 * Android bağımlılığı yoktur; birim testlerle doğrulanır.
 */
object QuietHoursEvaluator {

    const val MINUTES_IN_DAY = 24 * 60

    fun currentMinutesOfDay(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    /**
     * [nowMinutes] değerinin sessiz saat penceresinde olup olmadığı.
     *
     * - start < end: aynı gün, [start, end)  örn. 09:00–17:00
     * - start > end: gece yarısını saran aralık, örn. 22:00–08:00
     *   (now >= start VEYA now < end)
     * - start == end: boş pencere (sessiz saat yok)
     *
     * Başlangıç dahil, bitiş hariç tutulur; böylece 08:00'de kural kapanır
     * ve ertelenmiş bildirimler tam o anda gösterilebilir.
     */
    fun isInQuietHours(
        nowMinutes: Int,
        startMinutes: Int,
        endMinutes: Int
    ): Boolean {
        val now = ((nowMinutes % MINUTES_IN_DAY) + MINUTES_IN_DAY) % MINUTES_IN_DAY
        val start = normalize(startMinutes)
        val end = normalize(endMinutes)
        if (start == end) return false
        return if (start < end) {
            now >= start && now < end
        } else {
            now >= start || now < end
        }
    }

    /**
     * Sessiz saatin bir sonraki bitiş anının epoch millis değeri.
     * [now] sessiz saat dışındaysa bile bir sonraki bitişe (bugün veya yarın) gider.
     */
    fun nextQuietEndMillis(
        startMinutes: Int,
        endMinutes: Int,
        now: Calendar = Calendar.getInstance()
    ): Long {
        val start = normalize(startMinutes)
        val end = normalize(endMinutes)
        val cal = now.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, end / 60)
        cal.set(Calendar.MINUTE, end % 60)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val nowMinutes = currentMinutesOfDay(now)
        if (start > end) {
            // Gece yarısını saran aralık: sabah dilimindeysek bitiş bugün, aksi halde yarın.
            if (nowMinutes >= end) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            // Aynı gün: bitiş henüz gelmediyse bugün, aksi halde yarın.
            if (!cal.after(now)) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }

    fun formatMinutes(totalMinutes: Int): String {
        val normalized = normalize(totalMinutes)
        return "%02d:%02d".format(normalized / 60, normalized % 60)
    }

    /** Cumartesi / Pazar — "Hafta sonu kapat" açıkken sessiz saat uygulanmaz. */
    fun isWeekend(calendar: Calendar = Calendar.getInstance()): Boolean {
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY
    }

    private fun normalize(minutes: Int): Int {
        return ((minutes % MINUTES_IN_DAY) + MINUTES_IN_DAY) % MINUTES_IN_DAY
    }
}
