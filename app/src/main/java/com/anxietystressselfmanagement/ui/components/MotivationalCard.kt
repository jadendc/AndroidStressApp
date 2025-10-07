package com.example.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun MotivationalCard(
    userName: String? = null,
    modifier: Modifier = Modifier,
    message: String? = null,
    emoji: String = "🌟",
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val defaultMessage = if (!userName.isNullOrEmpty()) {
        "Keep going, $userName! You're doing great 🎉"
    } else {
        "Stay motivated! $emoji"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = message ?: defaultMessage,
            modifier = Modifier.padding(16.dp),
            style = textStyle
        )
    }
}
