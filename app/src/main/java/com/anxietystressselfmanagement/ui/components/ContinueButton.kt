package com.anxietystressselfmanagement.ui.components

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anxietystressselfmanagement.DashboardActivity
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.model.StrategyAction
import com.anxietystressselfmanagement.viewmodel.StrategyViewModel

@Composable
fun ContinueButton(
    selectedStrategy: String,
    selectedAction: String,
    selectedDate: String,
    viewModel: StrategyViewModel = viewModel()
) {
    val context = LocalContext.current
    Button(
        onClick = {
            val data = StrategyAction(selectedStrategy, selectedAction)
            viewModel.saveStrategyAndAction(
                data, selectedDate,
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
        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.button_grey)),
        shape = RectangleShape,
        modifier = Modifier.padding(top = 14.dp)
    ) {
        Text("CONTINUE")
    }
}
