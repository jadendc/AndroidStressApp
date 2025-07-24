package com.anxietystressselfmanagement.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.anxietystressselfmanagement.DashboardActivity
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.ui.activities.StrategiesActionsActivity
import com.anxietystressselfmanagement.ui.activities.SymptomSelectionActivity
import com.anxietystressselfmanagement.ui.components.BackButton
import com.anxietystressselfmanagement.ui.components.DefaultButton
import com.anxietystressselfmanagement.viewmodel.AwarenessViewModel
import com.anxietystressselfmanagement.viewmodel.SessionViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun AwarenessMainScreen(
    selectedDate: String,
    viewModel: AwarenessViewModel = viewModel()
) {
    val activity = LocalContext.current as? Activity
    val list: List<String> = listOf("Body", "Mind", "Feelings", "Behavior")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.grey)),
    ) {
        // Back navigation button
        BackButton { activity?.finish() }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxWidth()
        ) {
            Image(
                painterResource(id = R.drawable.logo),
                contentDescription = "Logo Picture",
                modifier = Modifier.width(120.dp)
            )

            Text(
                text = "Stressor = Body Reaction + Situation",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            Text(
                text = "Anxiety = Anticipation + Event (real or imagined)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "What are the signs?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            list.forEach { option ->
                DefaultButton(
                    label = viewModel.getIconFor(option) + option,
                    onClick = {
                        viewModel.selectedSign = option
                        val intent = Intent(activity, SymptomSelectionActivity::class.java)
                        intent.putExtra("selectedDate", selectedDate)
                        intent.putExtra("selectedSign", option)
                        activity?.startActivity(intent)
                        if (activity is Activity) activity.finish()
                    },
                    modifier = Modifier
                        .width(300.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DefaultButton(
                label = "CONTINUE",
                onClick = {
                    val intent = Intent(activity, StrategiesActionsActivity::class.java)
                    intent.putExtra("selectedDate", selectedDate)
                    activity?.startActivity(intent)
                    if (activity is Activity) activity.finish()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AwarenessMainPreviewScreen(

) {
//    AwarenessMainScreen(
//        selectedDate = "2025-03-10"
//    )
}