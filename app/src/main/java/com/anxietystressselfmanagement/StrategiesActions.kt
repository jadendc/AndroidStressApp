package com.anxietystressselfmanagement

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.serialization.json.Json
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.anxietystressselfmanagement.model.ActionDescription
import com.anxietystressselfmanagement.model.StrategyAction
import com.anxietystressselfmanagement.viewmodel.StrategyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

typealias ActionMap = Map<String, List<ActionDescription>>

class StrategiesActions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedDate = intent.getStringExtra("selectedDate")
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        setContent {
            MainView(selectedDate)
        }
    }

    companion object {
        fun newIntent(context: Context, selectedDate: String): Intent {
            return Intent(context, StrategiesActions::class.java).apply {
                putExtra("selectedDate", selectedDate)
            }
        }
    }

    @Composable
    fun MainView(selectedDate: String, viewModel: StrategyViewModel = viewModel()) {

        val context = LocalContext.current
        val selectedStrategy = viewModel.selectedStrategy
        val selectedAction = viewModel.selectedAction
        val strategies = viewModel.strategies
        val actions = viewModel.actions

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(id = R.color.grey))

        ) {
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

                DropdownSelector(
                    label = "Stress Management Strategies",
                    options = strategies,
                    selectedOption  = selectedStrategy,
                    onOptionSelected  = {
                        viewModel.selectedStrategy = it
                        viewModel.selectedAction = null
                    }
                )
                DropdownSelector(
                    label = "Stress Management Actions",
                    options = actions,
                    selectedOption  = selectedAction,
                    onOptionSelected  = {
                        viewModel.selectedAction = it
                    },
                    enabled = selectedStrategy != null
                )

                Button(
                    onClick = {
                        val data = StrategyAction(
                            selectedStrategy.toString(),
                            selectedAction.toString()
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        colorResource(id = R.color.button_grey)
                    ),
                    shape = RectangleShape,
                    modifier = Modifier.padding(top = 14.dp)
                ) {
                    Text("CONTINUE")
                }

            }
        }
    }

    @SuppressLint("ResourceAsColor")
    @Composable
    fun DropdownSelector(
        label: String,
        options: List<String>,
        selectedOption : String?,
        onOptionSelected : (String) -> Unit,
        enabled: Boolean = true
    ) {
        var expanded by remember { mutableStateOf(false) }
        var cardWidth by remember { mutableIntStateOf(0) }

        Box (
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
                        .clickable (enabled = enabled){
                            expanded = true
                        },
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.button_grey).copy(alpha = if (enabled) 1f else 0.6f)
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
                        .width(with(LocalDensity.current) {cardWidth.toDp() })
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
}