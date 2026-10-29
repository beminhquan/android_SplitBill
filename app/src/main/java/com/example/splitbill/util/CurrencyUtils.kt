package com.example.splitbill.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyUtils {

    private val vietnameseLocale = Locale("vi", "VN")
    private val numberFormat = NumberFormat.getNumberInstance(vietnameseLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    /**
     * Formats double to standard Vietnamese Dong string (e.g. "150.000 ₫")
     */
    fun formatVnd(amount: Double): String {
        return "${numberFormat.format(amount)} ₫"
    }

    /**
     * Formats signed VND for balance indicators (e.g. "+150.000 ₫", "-50.000 ₫", "0 ₫")
     */
    fun formatSignedVnd(amount: Double): String {
        return when {
            amount > 0.01 -> "+${formatVnd(amount)}"
            amount < -0.01 -> "-${formatVnd(kotlin.math.abs(amount))}"
            else -> "0 ₫"
        }
    }

    /**
     * Parses user input text into clean numeric Double
     */
    fun parseAmount(input: String): Double? {
        val clean = input.replace(".", "").replace(",", "").replace(" ", "").trim()
        return clean.toDoubleOrNull()
    }

    /**
     * Formats timestamp to readable Vietnamese date string
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", vietnameseLocale)
        return sdf.format(Date(timestamp))
    }

    /**
     * Formats timestamp to readable Vietnamese date & time string
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", vietnameseLocale)
        return sdf.format(Date(timestamp))
    }
}
