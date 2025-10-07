package com.anxietystressselfmanagement.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutText(
    text: String = "HowRU is a mental wellness app designed to help users manage anxiety and stress. It features a daily mood diary to track emotional patterns and guided breathing exercises to quickly reduce anxiety, offering practical tools for building mindfulness and improving mental well-being."
) {
    Box(
        modifier = Modifier
            .background(Color(0xFF5D727C), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 33.sp,
            textAlign = TextAlign.Center
        )
    }
}
