package com.anxietystressselfmanagement.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.ui.activities.StrategiesEntryEditActivity
import com.anxietystressselfmanagement.ui.components.DetailsCard
import com.anxietystressselfmanagement.ui.components.TopNavigationBar
import com.anxietystressselfmanagement.viewmodel.StrategiesDetailsViewModel

@Composable
fun StrategiesDetailsScreen(
    viewModel: StrategiesDetailsViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val entries = viewModel.entries
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.reloadEntries()  // Refresh after rating edit
        }
    }
    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Strategy Details",
                onBackClick = onBackClick
            )
        },
        containerColor = Color(0xFF8E9AAF)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(entries) { entry ->
                DetailsCard(
                    date = entry.date,
                    strategy = entry.strategy,
                    action = entry.action,
                    rating = entry.rating,
                    onClick = {
                        val intent = StrategiesEntryEditActivity.newIntent(context, entry)
                        launcher.launch(intent)
                    }
                )
            }
        }
    }
}

