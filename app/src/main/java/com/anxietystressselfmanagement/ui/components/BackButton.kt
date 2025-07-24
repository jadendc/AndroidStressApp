package com.anxietystressselfmanagement.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anxietystressselfmanagement.R

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
