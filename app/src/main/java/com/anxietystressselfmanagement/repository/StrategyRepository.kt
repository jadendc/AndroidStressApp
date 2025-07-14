package com.anxietystressselfmanagement.repository

import android.annotation.SuppressLint
import android.content.Context
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.model.ActionDescription
import com.anxietystressselfmanagement.model.StrategyAction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.serialization.json.Json

object StrategyRepository {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Loads data from strategies_actions.json
    fun loadStrategies(context: Context): Map<String, List<ActionDescription>> {
        val inputStream = context.resources.openRawResource(R.raw.strategies_actions)
        val json = inputStream.bufferedReader().use { it.readText() }
        return Json.decodeFromString(json)
    }

    fun saveStrategyAndAction(
        date: String,
        data: StrategyAction,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val data = mapOf(
            "7strategies" to data.strategy,
            "7actions" to data.action
        )

        db.collection("users")
            .document(currentUser.uid)
            .collection("dailyLogs")
            .document(date)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}