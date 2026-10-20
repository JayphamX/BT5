package com.example.coursemanager

import android.util.Patterns
import java.text.NumberFormat
import java.util.Locale

fun formatPrice(price: Long): String =
    NumberFormat.getInstance(Locale("vi", "VN")).format(price) + " đ"

fun isValidUrl(url: String): Boolean =
    (url.startsWith("http://") || url.startsWith("https://")) &&
        Patterns.WEB_URL.matcher(url).matches()

fun isYoutube(url: String): Boolean =
    url.contains("youtube.com", ignoreCase = true) || url.contains("youtu.be", ignoreCase = true)
