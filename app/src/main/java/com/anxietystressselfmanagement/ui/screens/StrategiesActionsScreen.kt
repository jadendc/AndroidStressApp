package com.anxietystressselfmanagement.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
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
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.ui.components.BackButton
import com.anxietystressselfmanagement.ui.components.ContinueButton
import com.anxietystressselfmanagement.ui.components.DropdownSelector
import com.anxietystressselfmanagement.viewmodel.StrategyViewModel

/**
 * Composable function that represents the full Strategies and Actions UI screen.
 * Handles state, UI layout, and interaction with the ViewModel.
 */
@SuppressLint("ContextCastToActivity")
@Composable
fun StrategiesActionsScreen(
    selectedDate: String,
    viewModel: StrategyViewModel = viewModel()
) {
    val selectedStrategy = viewModel.selectedStrategy
    val selectedAction = viewModel.selectedAction
    val strategies = viewModel.strategies
    val actions = viewModel.actions
    val activity = LocalContext.current as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.grey))
    ) {
        // Back navigation button
        BackButton { activity?.finish() }

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
                    viewModel.selectedStrategy = it
                    viewModel.selectedAction = null
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

            // Continue button to save and navigate
            ContinueButton(
                selectedStrategy = selectedStrategy.orEmpty(),
                selectedAction = selectedAction.orEmpty(),
                selectedDate = selectedDate
            )
        }
    }
}