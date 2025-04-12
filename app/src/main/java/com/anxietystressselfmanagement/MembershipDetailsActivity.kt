package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.regex.Pattern

class MembershipDetailsActivity : AppCompatActivity() {

    private lateinit var cardNumberEditText: EditText
    private lateinit var expiryDateEditText: EditText
    private lateinit var cvvEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership_details)

        // Initialize views
        cardNumberEditText = findViewById(R.id.cardNumber)
        expiryDateEditText = findViewById(R.id.expiryDate)
        cvvEditText = findViewById(R.id.cvv)
        val confirmButton = findViewById<Button>(R.id.confirmPayment)
        val backButton = findViewById<ImageView>(R.id.backButton)

        // Setup back button
        backButton.setOnClickListener {
            navigateToMembership()
        }

        // Setup confirm button with validation
        confirmButton.setOnClickListener {
            if (validateAllFields()) {
                // Only proceed if all validations pass
                Toast.makeText(this, "Payment details valid!", Toast.LENGTH_SHORT).show()
                navigateToMembership()
            }
        }
    }

    private fun validateAllFields(): Boolean {
        val cardNumber = cardNumberEditText.text.toString().replace(" ", "")
        val expiryDate = expiryDateEditText.text.toString()
        val cvv = cvvEditText.text.toString()

        return validateCardNumber(cardNumber) &&
                validateExpiryDate(expiryDate) &&
                validateCVV(cvv)
    }

    private fun validateCardNumber(cardNumber: String): Boolean {
        return when {
            cardNumber.length !in 13..19 -> {
                cardNumberEditText.error = "Invalid card number length"
                false
            }
            !cardNumber.matches(Regex("\\d+")) -> {
                cardNumberEditText.error = "Only numbers allowed"
                false
            }
            !isValidLuhn(cardNumber) -> {
                cardNumberEditText.error = "Invalid card number"
                false
            }
            else -> true
        }
    }

    private fun validateExpiryDate(expiryDate: String): Boolean {
        val pattern = Pattern.compile("^(0[1-9]|1[0-2])/?([0-9]{2})\$")
        val matcher = pattern.matcher(expiryDate)

        return when {
            !matcher.matches() -> {
                expiryDateEditText.error = "Use MM/YY format"
                false
            }
            else -> {
                val month = matcher.group(1)!!.toInt()
                val year = matcher.group(2)!!.toInt() + 2000
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) + 1 // Months are 0-based

                if (year < currentYear || (year == currentYear && month < currentMonth)) {
                    expiryDateEditText.error = "Card expired"
                    false
                } else {
                    true
                }
            }
        }
    }

    private fun validateCVV(cvv: String): Boolean {
        return when {
            cvv.length !in 3..4 -> {
                cvvEditText.error = "Invalid CVV length"
                false
            }
            !cvv.matches(Regex("\\d+")) -> {
                cvvEditText.error = "Only numbers allowed"
                false
            }
            else -> true
        }
    }

    // Luhn Algorithm implementation
    private fun isValidLuhn(number: String): Boolean {
        val digits = number.map { Character.getNumericValue(it) }.reversed()
        var sum = 0
        for (i in digits.indices) {
            var digit = digits[i]
            if (i % 2 == 1) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
        }
        return sum % 10 == 0
    }

    private fun navigateToMembership() {
        val intent = Intent(this, MembershipActivity::class.java)
        startActivity(intent)
        finish()
    }
}