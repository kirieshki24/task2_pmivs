package com.example.task2

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

enum class CompoundingFrequency(val periodsPerYear: Int) {
    MONTHLY(12),
    QUARTERLY(4),
    ANNUALLY(1)
}

data class FinancialInput(
    val principal: Double,
    val annualRatePercent: Double,
    val years: Int,
    val frequency: CompoundingFrequency
)

data class FinancialResult(
    val finalAmount: Double,
    val totalProfit: Double,
    val yearlyValues: List<Double>
)

data class HistoryEntry(
    val input: FinancialInput,
    val result: FinancialResult,
    val index: Int
)

fun validateInput(input: FinancialInput): List<String> {
    val errors = mutableListOf<String>()
    if (input.principal < 0) errors.add("Principal cannot be negative")
    if (input.principal > 1_000_000_000) errors.add("Principal exceeds maximum (1 billion)")
    if (input.annualRatePercent < 0) errors.add("Interest rate cannot be negative")
    if (input.annualRatePercent > 1000) errors.add("Interest rate is unrealistically high (> 1000%)")
    if (input.years <= 0) errors.add("Years must be greater than 0")
    if (input.years > 100) errors.add("Years cannot exceed 100")
    return errors
}

fun calculateCompoundInterest(input: FinancialInput): FinancialResult {
    val n = input.frequency.periodsPerYear.toDouble()
    val r = input.annualRatePercent / 100.0
    val t = input.years.toDouble()

    val finalAmount = input.principal * (1.0 + r / n).pow(n * t)
    val totalProfit = finalAmount - input.principal

    val yearlyValues = (0..input.years).map { year ->
        input.principal * (1.0 + r / n).pow(n * year.toDouble())
    }

    return FinancialResult(
        finalAmount = finalAmount,
        totalProfit = totalProfit,
        yearlyValues = yearlyValues
    )
}

fun Double.formatMoney(): String {
    val sign = if (this < 0) "-" else ""
    val absVal = abs(this)
    val rounded = (absVal * 100).roundToLong()
    val intPart = rounded / 100
    val decPart = rounded % 100
    return "$sign$intPart.${decPart.toString().padStart(2, '0')}"
}

object HistoryRepository {
    private val _entries = mutableListOf<HistoryEntry>()
    private var nextIndex = 0

    val entries: List<HistoryEntry> get() = _entries.toList()

    fun save(input: FinancialInput, result: FinancialResult): HistoryEntry {
        val entry = HistoryEntry(input, result, nextIndex++)
        _entries.add(0, entry)
        if (_entries.size > 50) _entries.removeAt(_entries.size - 1)
        return entry
    }

    fun load(): List<HistoryEntry> = entries

    fun clear() {
        _entries.clear()
    }
}
