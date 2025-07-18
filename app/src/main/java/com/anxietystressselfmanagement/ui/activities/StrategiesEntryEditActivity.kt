package com.anxietystressselfmanagement.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.anxietystressselfmanagement.model.StrategyCardEntry
import com.anxietystressselfmanagement.ui.screens.StrategiesEntryEditScreen

class StrategiesEntryEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract data from Intent
        val date = intent.getStringExtra("date") ?: ""
        val strategy = intent.getStringExtra("strategy") ?: ""
        val action = intent.getStringExtra("action") ?: ""
        val rating = intent.getIntExtra("rating", 0)

        val entry = StrategyCardEntry(date, strategy, action, rating)

        setContent {
            StrategiesEntryEditScreen(
                entry = entry,
                onBack = { finish() }
            )
        }
    }
    companion object {
        fun newIntent(context: Context, entry: StrategyCardEntry): Intent {
            return Intent(context, StrategiesEntryEditActivity::class.java).apply {
                putExtra("date", entry.date)
                putExtra("strategy", entry.strategy)
                putExtra("action", entry.action)
                putExtra("rating", entry.rating ?: 0)
            }
        }
    }
}