package com.anxietystressselfmanagement

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

/**
 * Singleton class to manage date range data across dashboard activities
 */
object DateRangeManager {
    // Keys for SharedPreferences
    private const val PREFS_NAME = "dashboard_prefs"
    private const val KEY_RANGE_TYPE = "range_type"
    private const val KEY_START_DATE = "start_date"
    private const val KEY_END_DATE = "end_date"

    // Default range type
    const val DEFAULT_RANGE = "Last 7 Days"

    /**
     * Save the current date range settings
     */
    fun saveDateRange(context: Context, rangeType: String, startCalendar: Calendar, endCalendar: Calendar) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_RANGE_TYPE, rangeType)
            putLong(KEY_START_DATE, startCalendar.timeInMillis)
            putLong(KEY_END_DATE, endCalendar.timeInMillis)
            apply()
        }
    }

    fun clearDateRange(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_RANGE_TYPE)
            .remove(KEY_START_DATE)
            .remove(KEY_END_DATE)
            .apply()
    }

    /**
     * Get the saved range type
     */
    fun getRangeType(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_RANGE_TYPE, DEFAULT_RANGE) ?: DEFAULT_RANGE
    }

    /**
     * Get the saved start date
     */
    fun getStartDate(context: Context): Calendar {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val startMillis = prefs.getLong(KEY_START_DATE, 0)

        val calendar = Calendar.getInstance()
        if (startMillis > 0) {
            calendar.timeInMillis = startMillis
        } else {
            // Default to 7 days ago if not set
            calendar.add(Calendar.DAY_OF_YEAR, -6)
        }

        return calendar
    }

    /**
     * Get the saved end date
     */
    fun getEndDate(context: Context): Calendar {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val endMillis = prefs.getLong(KEY_END_DATE, 0)

        val calendar = Calendar.getInstance()
        if (endMillis > 0) {
            calendar.timeInMillis = endMillis
        }

        return calendar
    }

    /**
     * Get a formatted string representation of the date range
     */
    fun getDateRangeDisplayText(context: Context): String {
        val rangeType = getRangeType(context)

        // For predefined ranges, we can just use the range type
        if (rangeType != "Custom Range") {
            return "Date Range: $rangeType"
        }

        // For custom range, format the dates
        val startDate = getStartDate(context)
        val endDate = getEndDate(context)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        return "Date Range: ${dateFormat.format(startDate.time)} - ${dateFormat.format(endDate.time)}"
    }

    /**
     * Get all date range information as a single object
     * @return DateRangeInfo object containing all date range settings
     */
    fun getDateRange(context: Context): DateRangeInfo? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if we have any saved date range data
        if (!prefs.contains(KEY_RANGE_TYPE)) {
            return null
        }

        // Get the individual components
        val rangeType = getRangeType(context)
        val startCalendar = getStartDate(context)
        val endCalendar = getEndDate(context)

        // Package them in a DateRangeInfo object
        return DateRangeInfo(
            rangeType = rangeType,
            startDate = startCalendar.time,
            endDate = endCalendar.time
        )
    }
}