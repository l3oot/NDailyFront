package com.ndaily.mobile.data

private val thaiMonths = listOf(
    "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
    "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.",
)

fun formatBirthdate(iso: String?, locale: AppLocale): String {
    if (iso.isNullOrBlank()) return "—"
    val date = iso.take(10)
    val parts = date.split("-")
    if (parts.size != 3) return "—"
    val year = parts[0]
    val month = parts[1].toIntOrNull() ?: return "—"
    val day = parts[2].toIntOrNull() ?: return "—"
    return if (locale == AppLocale.Th) {
        "$day ${thaiMonths.getOrNull(month - 1) ?: month} $year"
    } else {
        val names = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        "${names.getOrNull(month - 1) ?: month} $day, $year"
    }
}

fun nextThaiMobile(current: String, incoming: String): Pair<String, Boolean> {
    val digits = incoming.filter { it.isDigit() }.take(10)
    if (digits.isNotEmpty() && digits[0] != '0') return current to true
    if (digits.length >= 2 && digits[1] !in listOf('6', '8', '9')) return digits.take(1) to true
    return digits to false
}

fun isValidThaiMobile(value: String): Boolean = Regex("^0[689]\\d{8}$").matches(value.filter { it.isDigit() })

fun isValidEmail(value: String): Boolean = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value.trim())

private val hourSlots = listOf(0, 1, 3, 4, 6, 7, 9, 10)

fun nextOpenHours(prev: String, next: String): String {
    val oldDigits = prev.filter { it.isDigit() }.take(8)
    var digits = next.filter { it.isDigit() }.take(8)
    if (next.length < prev.length && digits.length == oldDigits.length && oldDigits.isNotEmpty()) {
        digits = oldDigits.dropLast(1)
    }
    val filtered = buildString {
        for (ch in digits) {
            if (length >= 8) break
            val cand = this.toString() + ch
            if (isValidHoursPrefix(cand)) append(ch)
        }
    }
    val chars = mutableListOf('_', '_', ':', '_', '_', '-', '_', '_', ':', '_', '_')
    filtered.forEachIndexed { i, c -> chars[hourSlots[i]] = c }
    return chars.joinToString("")
}

fun hoursForPayload(raw: String): String? {
    val s = raw.trim()
    if (s.isEmpty() || s == "__:__-__:__") return ""
    val d = s.filter { it.isDigit() }.take(8)
    if (d.isEmpty()) return ""
    if (d.length < 8) return ""
    if (!isValidHoursPrefix(d)) return null
    return "${d.substring(0, 2)}:${d.substring(2, 4)}-${d.substring(4, 6)}:${d.substring(6, 8)}"
}

fun displayHours(raw: String): String {
    val match = Regex("^(\\d{2}:\\d{2})-(\\d{2}:\\d{2})$").find(raw.trim()) ?: return ""
    return "${match.groupValues[1]}–${match.groupValues[2]}"
}

fun isOpenAllDay(raw: String): Boolean {
    val hours = displayHours(raw)
    return hours == "00:00–23:59" || hours == "00:00–00:00"
}

private fun isValidHoursPrefix(digits: String): Boolean {
    val len = digits.length
    if (len == 0) return true
    if (len >= 1 && digits[0].digitToInt() > 2) return false
    if (len >= 2 && digits.substring(0, 2).toInt() > 23) return false
    if (len >= 3 && digits[2].digitToInt() > 5) return false
    if (len >= 4 && digits.substring(2, 4).toInt() > 59) return false
    if (len >= 5 && digits[4].digitToInt() > 2) return false
    if (len >= 6 && digits.substring(4, 6).toInt() > 23) return false
    if (len >= 7 && digits[6].digitToInt() > 5) return false
    if (len >= 8 && digits.substring(6, 8).toInt() > 59) return false
    return true
}

fun starCount(score: Double): Double = if (score <= 0) 0.0 else score
