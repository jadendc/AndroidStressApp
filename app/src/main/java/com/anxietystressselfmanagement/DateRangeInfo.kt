package com.anxietystressselfmanagement

import java.util.Date

/**
 * Data class to encapsulate date range information
 * Using a data class gives us automatic equals(), hashCode(), toString(), and copy() methods
 */
data class DateRangeInfo(
    val rangeType: String,
    val startDate: Date,
    val endDate: Date
)