package com.example.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    private val localeVn = Locale("vi", "VN")
    
    fun formatVnd(amount: Double): String {
        val symbols = DecimalFormatSymbols(localeVn).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,##0", symbols)
        return "${df.format(amount)} đ"
    }

    fun formatNumber(value: Double): String {
        val symbols = DecimalFormatSymbols(localeVn).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,##0.##", symbols)
        return df.format(value)
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", localeVn)
        return sdf.format(Date(millis))
    }

    fun formatDateShort(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM", localeVn)
        return sdf.format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        val sdf = SimpleDateFormat("MM/yyyy", localeVn)
        return "Tháng ${sdf.format(Date(millis))}"
    }
}
