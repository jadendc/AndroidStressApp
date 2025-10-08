package com.anxietystressselfmanagement.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

// Data class for each FAQ item
data class FaqItem(val question: String, val answer: String)

// Main FAQ composable
@Composable
fun FaqText(
    faqList: List<FaqItem> = listOf(
        FaqItem("Question 1?", "Answer to question 1."),
        FaqItem("Question 2?", "Answer to question 2."),
        FaqItem("Question 3?", "Answer to question 3."),
        FaqItem("Question 4?", "Answer to question 4.")
    )
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        faqList.forEach { faq ->
            FaqCard(faq)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun FaqCard(faqItem: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { expanded = !expanded }, // No dark ripple
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF5D727C))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = faqItem.question,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faqItem.answer,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}
