package com.anxietystressselfmanagement.repository

import android.annotation.SuppressLint
import com.anxietystressselfmanagement.model.StrategyCardEntry
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

object StrategiesDetailsRepository {
    @SuppressLint("DefaultLocale")
    fun getStrategiesBetween(
        userId: String,
        startMillis: Long,
        endMillis: Long,
        onResult: (List<StrategyCardEntry>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userLogsRef = db.collection("users").document(userId).collection("dailyLogs")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startMillis

        val entries = mutableListOf<StrategyCardEntry>()

        val dateList = mutableListOf<String>()

        while (calendar.timeInMillis <= endMillis) {
            val dateStr = String.format(
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dateList.add(dateStr)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        var completedFetches = 0

        for (date in dateList) {
            userLogsRef.document(date).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val strategy = doc.getString("7strategies")
                        val action = doc.getString("7actions")
                        val rating = doc.getLong("strategyRating")?.toInt()

                        if (!strategy.isNullOrBlank() && !action.isNullOrBlank() && rating != null) {
                            entries.add(
                                StrategyCardEntry(
                                    date = date,
                                    strategy = strategy,
                                    action = action,
                                    rating = rating
                                )
                            )
                        }
                    }

                    completedFetches++
                    if (completedFetches == dateList.size) {
                        onResult(entries.sortedByDescending { it.date })
                    }
                }
                .addOnFailureListener {
                    completedFetches++
                    if (completedFetches == dateList.size) {
                        onResult(entries)
                    }
                }
        }
    }
}