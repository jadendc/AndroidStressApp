package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.regex.Pattern

class MembershipDetailsActivity : AppCompatActivity() {

    private lateinit var cardNumberEditText: EditText
    private lateinit var expiryDateEditText: EditText
    private lateinit var cvvEditText: EditText
    private lateinit var cardTypeLogo: ImageView
    private var currentCardType: CardType = CardType.UNKNOWN

    enum class CardType {
        VISA, MASTERCARD, AMEX, DISCOVER, UNKNOWN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership_details)

        // Initialize views
        cardNumberEditText = findViewById(R.id.cardNumber)
        expiryDateEditText = findViewById(R.id.expiryDate)
        cvvEditText = findViewById(R.id.cvv)
        cardTypeLogo = findViewById(R.id.cardTypeLogo)
        val confirmButton = findViewById<Button>(R.id.confirmPayment)
        val backButton = findViewById<ImageView>(R.id.backButton)

        // Setup back button
        backButton.setOnClickListener {
            navigateToMembership()
        }

        // Setup formatting and validation
        setupCardNumberFormatting()
        setupExpiryDateFormatting()

        // Confirm button click handler
        confirmButton.setOnClickListener {
            if (validateAllFields()) {
                AlertDialog.Builder(this)
                    .setTitle("Confirm Payment")
                    .setMessage("Are you sure you want to proceed with this payment?")
                    .setPositiveButton("Confirm") { _, _ ->
                        processPayment()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun setupCardNumberFormatting() {
        cardNumberEditText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deletingHyphen = false
            private val hyphenPositions = setOf(4, 9, 14) // Positions where spaces should be

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                val original = s.toString().replace(" ", "")
                detectCardType(original)
                updateCardLogo()

                isFormatting = true

                // Format with spaces every 4 characters
                val formatted = StringBuilder()
                for (i in original.indices) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ")
                    formatted.append(original[i])
                }

                // Calculate cursor position
                var cursorPos = cardNumberEditText.selectionStart
                val addedChars = (formatted.length - s?.length!!)
                cursorPos += addedChars

                cardNumberEditText.setText(formatted.toString())
                cardNumberEditText.setSelection(if (cursorPos > formatted.length) formatted.length else cursorPos)

                isFormatting = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deletingHyphen = count == 1 && after == 0 && s?.getOrNull(start) == ' '
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupExpiryDateFormatting() {
        expiryDateEditText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deleting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Set the deleting flag if characters are being removed
                deleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                // Remove all non-digits
                var clean = s.toString().replace(Regex("[^\\d]"), "")
                // Limit input to at most 4 digits (MMYY)
                if (clean.length > 4) clean = clean.substring(0, 4)

                // Format the expiry date based on the number of digits, but when deleting, avoid forcing the slash.
                val formatted = when {
                    clean.isEmpty() -> ""
                    clean.length < 3 -> {
                        // If not deleting, auto-append "/" when exactly 2 digits are present.
                        if (!deleting && clean.length == 2) "$clean/" else clean
                    }
                    else -> {
                        // For 3 or 4 digits, always insert slash between 2nd and 3rd digits.
                        clean.substring(0, 2) + "/" + clean.substring(2)
                    }
                }

                expiryDateEditText.setText(formatted)
                expiryDateEditText.setSelection(formatted.length)

                isFormatting = false
            }
        })
    }

    private fun detectCardType(number: String) {
        currentCardType = when {
            number.startsWith("4") -> CardType.VISA
            number.startsWith("5") && number.take(2).toIntOrNull() in 51..55 -> CardType.MASTERCARD
            number.startsWith("34") || number.startsWith("37") -> CardType.AMEX
            number.startsWith("6011") || number.startsWith("65") -> CardType.DISCOVER
            else -> CardType.UNKNOWN
        }
        adjustValidationRules()
    }

    private fun updateCardLogo() {
        val logoRes = when (currentCardType) {
            CardType.VISA -> R.drawable.ic_visa
            CardType.MASTERCARD -> R.drawable.ic_mastercard
            CardType.AMEX -> R.drawable.ic_amex
            CardType.DISCOVER -> R.drawable.ic_discover
            else -> null
        }

        if (logoRes != null) {
            cardTypeLogo.setImageResource(logoRes)
            cardTypeLogo.visibility = View.VISIBLE
        } else {
            cardTypeLogo.visibility = View.GONE
        }
    }

    private fun adjustValidationRules() {
        when (currentCardType) {
            CardType.AMEX -> {
                cvvEditText.hint = "CID (4 digits)"
                cvvEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(4))
            }
            else -> {
                cvvEditText.hint = "CVV (3 digits)"
                cvvEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))
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
            cardNumber.isEmpty() -> {
                cardNumberEditText.error = "Card number required"
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
            currentCardType == CardType.AMEX && cardNumber.length != 15 -> {
                cardNumberEditText.error = "Amex requires 15 digits"
                false
            }
            currentCardType != CardType.AMEX && cardNumber.length !in 13..16 -> {
                cardNumberEditText.error = "Invalid card number length"
                false
            }
            else -> true
        }
    }

    private fun validateExpiryDate(expiryDate: String): Boolean {
        val pattern = Pattern.compile("^(0[1-9]|1[0-2])/?([0-9]{2})\$")
        val matcher = pattern.matcher(expiryDate)

        return when {
            expiryDate.isEmpty() -> {
                expiryDateEditText.error = "Expiry date required"
                false
            }
            !matcher.matches() -> {
                expiryDateEditText.error = "Use MM/YY format"
                false
            }
            else -> {
                val month = matcher.group(1)!!.toInt()
                val year = matcher.group(2)!!.toInt() + 2000
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) + 1

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
            cvv.isEmpty() -> {
                cvvEditText.error = "CVV required"
                false
            }
            !cvv.matches(Regex("\\d+")) -> {
                cvvEditText.error = "Only numbers allowed"
                false
            }
            currentCardType == CardType.AMEX && cvv.length != 4 -> {
                cvvEditText.error = "Amex requires 4-digit CID"
                false
            }
            currentCardType != CardType.AMEX && cvv.length != 3 -> {
                cvvEditText.error = "CVV must be 3 digits"
                false
            }
            else -> true
        }
    }

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

    private fun processPayment() {
        Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show()
        navigateToMembership()
    }

    private fun navigateToMembership() {
        val intent = Intent(this, MembershipActivity::class.java)
        startActivity(intent)
        finish()
    }
}