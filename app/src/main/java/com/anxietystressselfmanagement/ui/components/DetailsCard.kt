package com.anxietystressselfmanagement.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DetailsCard(
    date: String,
    strategy: String,
    action: String,
    rating: Int? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)) // Dark background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📅 $date", style = MaterialTheme.typography.labelMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Strategy: $strategy", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("Action: $action", style = MaterialTheme.typography.bodyMedium, color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))
            if(rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rating:", color = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    repeat(rating) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = Color.Yellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    repeat(5 - rating) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "Empty Star",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Not rated",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardPreview() {
    DetailsCard(
        date = "2025-01-01",
        strategy = "Physical",
        action = "Cold Shower",
        rating = 3,
        onClick = {}
    )
}