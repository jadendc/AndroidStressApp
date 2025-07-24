package com.anxietystressselfmanagement.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CustomStrategyDialog(
    strategyInput: String,
    actionInput: String,
    onStrategyChange: (String) -> Unit,
    onActionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Strategy & Action") },
        text = {
            Column {
                OutlinedTextField(
                    value = strategyInput,
                    onValueChange = onStrategyChange,
                    label = { Text("Custom Strategy") }
                )
                OutlinedTextField(
                    value = actionInput,
                    onValueChange = onActionChange,
                    label = { Text("Custom Action") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomStrategyDialog() {
    var strategyInput by remember { mutableStateOf("Meditation") }
    var actionInput by remember { mutableStateOf("5 minutes before bed") }
    var isDialogVisible by remember { mutableStateOf(true) }

    if (isDialogVisible) {
        CustomStrategyDialog(
            strategyInput = strategyInput,
            actionInput = actionInput,
            onStrategyChange = { strategyInput = it },
            onActionChange = { actionInput = it },
            onConfirm = { isDialogVisible = false },
            onDismiss = { isDialogVisible = false }
        )
    }
}
