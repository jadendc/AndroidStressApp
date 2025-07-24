package com.anxietystressselfmanagement.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.model.AwarenessSigns
import com.anxietystressselfmanagement.ui.activities.AwarenessActivity
import com.anxietystressselfmanagement.ui.components.BackButton
import com.anxietystressselfmanagement.ui.components.DefaultButton
import com.anxietystressselfmanagement.ui.components.DropdownSelector
import com.anxietystressselfmanagement.viewmodel.AwarenessViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun SymptomSelectorScreen(
    selectedDate: String,
    selectedSign: String,
    viewModel: AwarenessViewModel = viewModel(),
) {

    // Set the selected sign
    LaunchedEffect(Unit) {
        viewModel.selectedSign = selectedSign.toString()
    }


    val activity = LocalContext.current as? Activity
    val awarenessOptions = viewModel.awarenessOptions
    val selectedOption = viewModel.selectedOption

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.grey))
    ) {
        // Back navigation button
        BackButton { activity?.finish() }

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
                text = viewModel.selectedSign.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            
            DropdownSelector(
                label = "",
                defaultBarMessage = "This may look like...",
                options = awarenessOptions,
                selectedOption = selectedOption,
                onOptionSelected = {
                    viewModel.selectedOption = it
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultButton(
                label = "SAVE",
                modifier = Modifier.padding(14.dp),
                onClick = {
                    viewModel.saveAwarenessSelection(
                        data = AwarenessSigns(selectedSign, selectedOption.orEmpty()),
                        date = selectedDate.toString(),
                        onSuccess = {
                            Toast.makeText(activity, "Saved successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(activity, AwarenessActivity::class.java)
                            intent.putExtra("selectedDate", selectedDate)
                            activity?.startActivity(intent)
                            if (activity is Activity) activity.finish()
                        },
                        onFailure = {
                            Toast.makeText(activity, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}

@Preview()
@Composable
fun AwarenessScreenPreview() {

//    SignsSelectorScreen(
//        selectedDate = "2025-03-10",
//        navController = null
//    )
}