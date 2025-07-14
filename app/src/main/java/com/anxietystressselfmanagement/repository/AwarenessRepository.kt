package com.anxietystressselfmanagement.repository

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.text.toLowerCase
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.model.ActionDescription
import com.anxietystressselfmanagement.model.AwarenessSigns
import com.anxietystressselfmanagement.model.StrategyAction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.serialization.json.Json

object AwarenessRepository {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Loads data from awareness_act.json
    fun loadAwareness(context: Context): Map<String, List<String>> {
        val inputStream = context.resources.openRawResource(R.raw.awareness_act)
        val json = inputStream.bufferedReader().use { it.readText() }
        return Json.decodeFromString(json)
    }

    // Saves awareness choices to FirebaseDB
    fun saveAwarenessChoice(
        date: String,
        data: AwarenessSigns,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = AwarenessRepository.auth.currentUser

        if (currentUser == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val data = mapOf(
            data.sign.toString().lowercase() + "Symptom" to data.symptom
        )

        AwarenessRepository.db.collection("users")
            .document(currentUser.uid)
            .collection("dailyLogs")
            .document(date)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}