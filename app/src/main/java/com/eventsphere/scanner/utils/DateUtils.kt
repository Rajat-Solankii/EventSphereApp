package com.eventsphere.scanner.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    fun formatEventDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "TBA"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(dateString) ?: return dateString
            
            val formatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
            formatter.timeZone = TimeZone.getDefault()
            formatter.format(date)
        } catch (e: Exception) {
            try {
                // Try fallback parser without milliseconds
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(dateString) ?: return dateString
                
                val formatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
                formatter.timeZone = TimeZone.getDefault()
                formatter.format(date)
            } catch (e2: Exception) {
                dateString
            }
        }
    }
    
    fun getEventTimeMillis(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) return 0L
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            parser.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                parser.parse(dateString)?.time ?: 0L
            } catch (e2: Exception) {
                0L
            }
        }
    }
}
