package com.anxietystressselfmanagement.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.model.StrategyCardEntry
import com.anxietystressselfmanagement.ui.components.DefaultButton
import com.anxietystressselfmanagement.ui.components.TopNavigationBar
import com.anxietystressselfmanagement.viewmodel.AwarenessViewModel
import com.anxietystressselfmanagement.viewmodel.StrategiesDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategiesEntryEditScreen(
    entry: StrategyCardEntry,
    onBack: () -> Unit,
    viewModel: StrategiesDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(entry.rating ?: 0) }

    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Strategy Details",
                onBackClick = onBack
            )
        },
        containerColor = colorResource(id = R.color.grey)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("📅 Date: ${entry.date}", color = Color.White)
            Text("Strategy: ${entry.strategy}", color = Color.White)
            Text("Action: ${entry.action}", color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate this strategy",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {}
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "$i Star",
                            tint = if (i <= rating) Color.Yellow else Color.Gray,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = i }
                                .padding(4.dp)
                                .animateContentSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DefaultButton(
                    onClick = {
                        viewModel.updateRatingForEntry(
                            date = entry.date,
                            newRating = rating,
                            onSuccess = {
                                Toast.makeText(context, "Rating saved!", Toast.LENGTH_SHORT).show()
                                if (context is Activity) {
                                    context.setResult(Activity.RESULT_OK)
                                    context.finish()
                                }
                            },
                            onFailure = {
                                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    label = "Save Rating"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStrategyRatingScreen() {
    val mockEntry = StrategyCardEntry(
        date = "2025-07-14",
        strategy = "Deep Breathing",
        action = "Practice for 5 minutes before bed",
        rating = 3
    )

    StrategiesEntryEditScreen(
        entry = mockEntry,
        onBack = {},
    )
}
