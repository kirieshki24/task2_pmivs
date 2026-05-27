package com.example.task2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FinancialCalculatorState {
    var principalText by mutableStateOf("10000")
        private set
    var rateText by mutableStateOf("5.0")
        private set
    var yearsText by mutableStateOf("10")
        private set
    var frequency by mutableStateOf(CompoundingFrequency.MONTHLY)
        private set
    var result by mutableStateOf<FinancialResult?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var history by mutableStateOf<List<HistoryEntry>>(emptyList())
        private set
    var language by mutableStateOf(Language.ENGLISH)
        private set

    fun onPrincipalChange(value: String) {
        principalText = value
        errorMessage = null
    }

    fun onRateChange(value: String) {
        rateText = value
        errorMessage = null
    }

    fun onYearsChange(value: String) {
        yearsText = value
        errorMessage = null
    }

    fun onFrequencyChange(freq: CompoundingFrequency) {
        frequency = freq
    }

    fun onLanguageChange(lang: Language) {
        language = lang
    }

    fun calculate(strings: AppStrings) {
        try {
            val principal = principalText.trim().toDoubleOrNull()
            if (principal == null) {
                errorMessage = strings.errorInvalidNumber
                println("Validation error: invalid principal input '${principalText}'")
                return
            }
            val rate = rateText.trim().toDoubleOrNull()
            if (rate == null) {
                errorMessage = strings.errorInvalidNumber
                println("Validation error: invalid rate input '${rateText}'")
                return
            }
            val years = yearsText.trim().toIntOrNull()
            if (years == null) {
                errorMessage = strings.errorInvalidNumber
                println("Validation error: invalid years input '${yearsText}'")
                return
            }
            val input = FinancialInput(principal, rate, years, frequency)
            val errors = validateInput(input)
            if (errors.isNotEmpty()) {
                errorMessage = mapValidationError(errors.first(), strings)
                println("Validation errors: $errors")
                return
            }
            val calc = calculateCompoundInterest(input)
            result = calc
            errorMessage = null
            HistoryRepository.save(input, calc)
            history = HistoryRepository.load()
        } catch (e: Exception) {
            errorMessage = strings.errorInvalidNumber
            println("Calculation exception: ${e.message}")
        }
    }

    fun clearHistory() {
        HistoryRepository.clear()
        history = emptyList()
    }

    private fun mapValidationError(error: String, strings: AppStrings): String = when {
        error.contains("negative") && error.contains("rincipal") -> strings.errorNegativePrincipal
        error.contains("negative") && error.contains("rate") -> strings.errorNegativeRate
        error.contains("Years") || error.contains("greater") || error.contains("exceed 100") -> strings.errorYearsRange
        error.contains("unrealistically") -> strings.errorRateTooHigh
        error.contains("billion") || error.contains("maximum") -> strings.errorPrincipalTooLarge
        else -> error
    }
}
