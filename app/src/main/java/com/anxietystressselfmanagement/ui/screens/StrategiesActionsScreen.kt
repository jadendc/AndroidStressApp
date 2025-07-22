package com.anxietystressselfmanagement.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anxietystressselfmanagement.DashboardActivity
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.model.StrategyAction
import com.anxietystressselfmanagement.ui.components.BackButton
import com.anxietystressselfmanagement.ui.components.CustomStrategyDialog
import com.anxietystressselfmanagement.ui.components.DefaultButton
import com.anxietystressselfmanagement.ui.components.DropdownSelector
import com.anxietystressselfmanagement.ui.components.StarRating
import com.anxietystressselfmanagement.viewmodel.StrategiesActionViewModel

/**
 * Composable function that represents the full Strategies and Actions UI screen.
 * Handles state, UI layout, and interaction with the ViewModel.
 */
@SuppressLint("ContextCastToActivity")
@Composable
fun StrategiesActionsScreen(
    selectedDate: String,
    viewModel: StrategiesActionViewModel = viewModel()
) {
    val selectedStrategy = viewModel.selectedStrategy
    val selectedAction = viewModel.selectedAction
    val strategies = viewModel.strategies
    val actions = viewModel.actions
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current

    val isContinuedEnabled = !selectedStrategy.isNullOrBlank() && !selectedAction.isNullOrBlank()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.grey))
    ) {
        // Back navigation button
        BackButton { activity?.finish() }

        if (viewModel.showCustomDialog) {
            CustomStrategyDialog(
                strategyInput = viewModel.customStrategyInput,
                actionInput = viewModel.customActionInput,
                onStrategyChange = { viewModel.customStrategyInput = it },
                onActionChange = { viewModel.customActionInput = it },
                onConfirm = { viewModel.onCustomConfirm() },
                onDismiss = { viewModel.showCustomDialog = false }
            )
        }

        // Main layout
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 15.dp)
        ) {
            Image(
                painterResource(id = R.drawable.logo),
                contentDescription = "Logo Picture",
                modifier = Modifier.width(120.dp)
            )

            Text(
                text = "Select Strategies and Actions",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            // First dropdown for strategies
            DropdownSelector(
                label = "Stress Management Strategies",
                options = strategies,
                selectedOption = selectedStrategy,
                onOptionSelected = {
                    if (it == "Custom") {
                        viewModel.showCustomDialog = true
                    } else {
                        viewModel.selectedStrategy = it
                        viewModel.selectedAction = null
                    }
                }
            )

            // Second dropdown for actions (enabled only if strategy selected)
            DropdownSelector(
                label = "Stress Management Actions",
                options = actions,
                selectedOption = selectedAction,
                onOptionSelected = {
                    viewModel.selectedAction = it
                },
                enabled = selectedStrategy != null
            )

            StarRating(
                selectedRating = viewModel.selectedRating,
                onRatingSelected = {viewModel.selectedRating = it }
            )


            // Continue button to save and navigate
            DefaultButton(
                label = "CONTINUE",
                modifier = Modifier.padding(14.dp),
                onClick = {
                    if (isContinuedEnabled) {
                        viewModel.saveStrategyAndAction(
                            data = StrategyAction(selectedStrategy, selectedAction),
                            rating = viewModel.selectedRating,
                            date = selectedDate,
                            onSuccess = {
                                Toast.makeText(activity, "Saved successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(activity, DashboardActivity::class.java)
                                intent.putExtra("selectedDate", selectedDate)
                                activity?.startActivity(intent)
                                if (activity is Activity) activity.finish()
                            },
                            onFailure = {
                                Toast.makeText(activity, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Please select a strategy and an action before continuing.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }
}