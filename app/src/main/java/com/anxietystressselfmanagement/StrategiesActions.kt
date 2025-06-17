package com.anxietystressselfmanagement

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlin.math.exp

@Serializable
data class ActionDescriptions(
    val action: String,
    val details: String
)

typealias ActionMap = Map<String, List<ActionDescriptions>>

class StrategiesActions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainView()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainView() {

        val context = LocalContext.current
        var jsonContent by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            val inputStream = context.resources.openRawResource(R.raw.strategies_actions)
            jsonContent = inputStream.bufferedReader().use { it.readText() }
        }

        val parsedMap: Map<String, List<ActionDescriptions>>? = jsonContent?.let {
            Json.decodeFromString(it)
        }

        val categories = parsedMap?.keys?.toList() ?: emptyList()

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

                InputDropdownMenu(
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    label = "Stress Management Strategies",
                    title = "Random thing",
                    options = categories
                )
                InputDropdownMenu(
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    label = "Stress Management Actions",
                    title = "Random thing",
                    options = listOf("one", "two", "three")
                )
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    @Composable
    fun InputDropdownMenu(
        modifier: Modifier,
        label: String,
        title: String,
        options: List<String>,
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf("Select one...") }
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
                        .clickable { expanded = true },
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.button_grey)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = selectedOption,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }

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
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}