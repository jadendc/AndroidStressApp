package com.anxietystressselfmanagement.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.ui.components.DetailsCard
import com.anxietystressselfmanagement.ui.components.TopNavigationBar
import com.anxietystressselfmanagement.viewmodel.StrategiesDetailsViewModel

@Composable
fun StrategiesDetailsScreen(
    viewModel: StrategiesDetailsViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val entries = viewModel.entries

    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Strategy Details",
                onBackClick = onBackClick
            )
        },
        containerColor = colorResource(id = R.color.grey)
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
                    rating = entry.rating
                )
            }
        }
    }
}

