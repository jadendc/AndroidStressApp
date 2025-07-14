package com.anxietystressselfmanagement.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anxietystressselfmanagement.R


@Composable
fun DefaultButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.button_grey)
        ),
        shape = RectangleShape,
        modifier = modifier
    ) {
        Text(label)
    }
}
