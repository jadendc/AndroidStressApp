package com.anxietystressselfmanagement.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anxietystressselfmanagement.ui.screens.StrategiesDetailsScreen
import com.anxietystressselfmanagement.viewmodel.StrategiesDetailsViewModel


class StrategiesDetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startMillis = intent.getLongExtra("startMillis", 0L)
        val endMillis = intent.getLongExtra("endMillis", 0L)

        setContent {
            val viewModel: StrategiesDetailsViewModel = viewModel()

            LaunchedEffect(Unit) {
                viewModel.fetchStrategiesBetween(startMillis, endMillis)
            }

            StrategiesDetailsScreen(
                viewModel = viewModel,
                onBackClick = { finish() }
            )
        }
    }

    companion object {
        fun newIntent(context: Context, startMillis: Long, endMillis: Long): Intent {
            return Intent(context, StrategiesDetailsActivity::class.java).apply {
                putExtra("startMillis", startMillis)
                putExtra("endMillis", endMillis)
            }
        }
    }
}
