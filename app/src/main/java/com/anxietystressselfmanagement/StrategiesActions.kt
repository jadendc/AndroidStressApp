package com.anxietystressselfmanagement

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anxietystressselfmanagement.model.StrategyAction
import com.anxietystressselfmanagement.viewmodel.StrategyViewModel
import java.text.SimpleDateFormat
import java.util.*

class StrategiesActions : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedDate = intent.getStringExtra("selectedDate")
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        setContent {
            MainView(selectedDate)
        }
    }

    @SuppressLint("ContextCastToActivity")
    @Composable
    fun MainView(selectedDate: String) {
        val viewModel: StrategyViewModel = viewModel()
        val selectedStrategy = viewModel.selectedStrategy
        val selectedAction = viewModel.selectedAction
        val strategies = viewModel.strategies
        val actions = viewModel.actions
        val activity = LocalContext.current as? Activity
        var rating by remember { mutableStateOf(0) }

        val isButtonEnabled = !selectedStrategy.isNullOrBlank() && !selectedAction.isNullOrBlank() && rating > 0


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(id = R.color.grey))
        ) {
            BackButton {
                activity?.finish()
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 15.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Picture",
                    modifier = Modifier.width(120.dp)
                )

                Text(
                    text = "Select Strategies and Actions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )

                DropdownSelector(
                    label = "Stress Management Strategies",
                    options = strategies,
                    selectedOption = selectedStrategy,
                    onOptionSelected = {
                        viewModel.selectedStrategy = it
                        viewModel.selectedAction = null
                    }
                )

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
                    selectedRating = rating,
                    onRatingSelected = { rating = it }
                )

                ContinueButton(
                    selectedStrategy = selectedStrategy,
                    selectedAction = selectedAction,
                    selectedDate = selectedDate,
                    rating = rating
                )

                if (!isButtonEnabled) {
                    val message = when {
                        selectedStrategy == null -> "Please select a strategy."
                        selectedAction == null -> "Please select an action."
                        rating == 0 -> "Please provide a rating."
                        else -> ""
                    }

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DropdownSelector(
        label: String,
        options: List<String>,
        selectedOption: String?,
        onOptionSelected: (String) -> Unit,
        enabled: Boolean = true
    ) {
        var expanded by remember { mutableStateOf(false) }
        var cardWidth by remember { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            cardWidth = coordinates.size.width
                        }
                        .clickable(enabled = enabled) {
                            expanded = true
                        },
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.button_grey)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = selectedOption ?: "Select one...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }

            if (enabled) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { cardWidth.toDp() })
                        .background(colorResource(id = R.color.button_grey))
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ContinueButton(
        selectedStrategy: String?,
        selectedAction: String?,
        rating: Int,
        selectedDate: String,
        viewModel: StrategyViewModel = viewModel()
    ) {
        val context = LocalContext.current
        val isEnabled = !selectedStrategy.isNullOrBlank() && !selectedAction.isNullOrBlank() && rating > 0


        Button(
            onClick = {
                when {
                    selectedStrategy.isNullOrBlank() -> Toast.makeText(context, "Please select a strategy", Toast.LENGTH_SHORT).show()
                    selectedAction.isNullOrBlank() -> Toast.makeText(context, "Please select an action", Toast.LENGTH_SHORT).show()
                    rating == 0 -> Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show()
                    else -> {
                        val data = StrategyAction(
                            selectedStrategy,
                            selectedAction,
                            rating
                        )
                        viewModel.saveStrategyAndAction(
                            data,
                            selectedDate,
                            onSuccess = {
                                Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, DashboardActivity::class.java)
                                intent.putExtra("selectedDate", selectedDate)
                                context.startActivity(intent)
                                if (context is Activity) context.finish()
                            },
                            onFailure = {
                                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            },
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) colorResource(id = R.color.button_grey) else Color.Gray
            ),
            shape = RectangleShape,
            modifier = Modifier.padding(top = 14.dp)
        ) {
            Text("CONTINUE")
        }
    }

    @Composable
    fun StarRating(
        selectedRating: Int,
        onRatingSelected: (Int) -> Unit
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Pick a rate of effectiveness",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= selectedRating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "$i Star",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .clickable { onRatingSelected(i) },
                        tint = if (i <= selectedRating) Color.Yellow else Color.Gray
                    )
                }
            }
        }
    }

    @Composable
    fun BackButton(onBack: () -> Unit) {
        Image(
            painter = painterResource(id = R.drawable.backbutton),
            contentDescription = "Back",
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onBack)
                .padding(8.dp)
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun CustomBackButtonPreview() {
        BackButton(onBack = {})
    }

    @Preview(showBackground = true)
    @Composable
    fun MainViewPreview() {
        MainView(selectedDate = "2025-06-23")
    }

    @Preview(showBackground = true)
    @Composable
    fun StarRatingPreview() {
        StarRating(1, {})
    }
}
