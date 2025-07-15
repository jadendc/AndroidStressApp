package com.anxietystressselfmanagement.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.model.StrategyCardEntry
import com.anxietystressselfmanagement.ui.components.DetailsCard
import com.anxietystressselfmanagement.ui.components.TopNavigationBar

@Composable
fun StrategiesDetailsScreen(
    entries: List<StrategyCardEntry>,
    onBackClick: () -> Unit
) {
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

@Preview(showBackground = true, heightDp = 600)
@Composable
fun PreviewStrategiesDetailsScreen() {
    val mockEntries = listOf(
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-13", "Meditation", "Morning session", 5),
        StrategyCardEntry("2025-07-12", "Journaling", "Write 3 thoughts before bed", 3),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
        StrategyCardEntry("2025-07-14", "Deep Breathing", "5 minutes before bed", 4),
    )

    StrategiesDetailsScreen(
        entries = mockEntries,
        onBackClick = {} // no-op for preview
    )
}
