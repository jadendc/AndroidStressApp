package com.anxietystressselfmanagement

// Data class to hold detailed sign information
data class SignDetail(
    val category: String,           // e.g., "Mind"
    val count: Int,               // Total count for this category in the range
    val specificSigns: List<String> // List of all specific signs/symptoms recorded for this category
)