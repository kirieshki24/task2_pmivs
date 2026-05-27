package com.example.task2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

// Integration tests: full calculate → save → load → clear flow
class IntegrationTest {

    @Test
    fun fullCalculateAndSaveFlow() {
        HistoryRepository.clear()
        val input = FinancialInput(10_000.0, 5.0, 10, CompoundingFrequency.MONTHLY)
        val errors = validateInput(input)
        assertTrue(errors.isEmpty())
        val result = calculateCompoundInterest(input)
        val entry = HistoryRepository.save(input, result)
        val loaded = HistoryRepository.load()
        assertEquals(1, loaded.size)
        assertEquals(entry.index, loaded.first().index)
        assertEquals(input.principal, loaded.first().input.principal, 0.001)
        assertEquals(result.finalAmount, loaded.first().result.finalAmount, 0.001)
    }

    @Test
    fun multipleCalculationsSaveInReverseOrder() {
        HistoryRepository.clear()
        val inputs = listOf(
            FinancialInput(1_000.0, 3.0, 5, CompoundingFrequency.ANNUALLY),
            FinancialInput(5_000.0, 7.0, 10, CompoundingFrequency.MONTHLY),
            FinancialInput(20_000.0, 10.0, 3, CompoundingFrequency.QUARTERLY)
        )
        inputs.forEach { HistoryRepository.save(it, calculateCompoundInterest(it)) }
        val history = HistoryRepository.load()
        assertEquals(3, history.size)
        // Most recent first
        assertEquals(inputs.last().principal, history.first().input.principal, 0.001)
        assertEquals(inputs.first().principal, history.last().input.principal, 0.001)
    }

    @Test
    fun clearHistoryRemovesAllEntries() {
        HistoryRepository.clear()
        repeat(5) { i ->
            val input = FinancialInput((i + 1) * 1000.0, 5.0, 5, CompoundingFrequency.MONTHLY)
            HistoryRepository.save(input, calculateCompoundInterest(input))
        }
        assertEquals(5, HistoryRepository.load().size)
        HistoryRepository.clear()
        assertTrue(HistoryRepository.load().isEmpty())
    }

    @Test
    fun stateCalculateThenChangeLanguage() {
        HistoryRepository.clear()
        val state = FinancialCalculatorState()
        state.onPrincipalChange("15000")
        state.onRateChange("6.5")
        state.onYearsChange("8")
        state.calculate(englishStrings)
        assertNotNull(state.result)
        // switch language — result should remain, strings change
        state.onLanguageChange(Language.RUSSIAN)
        assertEquals(Language.RUSSIAN, state.language)
        assertNotNull(state.result)  // result persists after language change
    }

    @Test
    fun differentFrequenciesProduceDifferentResults() {
        HistoryRepository.clear()
        val base = FinancialInput(10_000.0, 12.0, 5, CompoundingFrequency.MONTHLY)
        val quarterly = FinancialInput(10_000.0, 12.0, 5, CompoundingFrequency.QUARTERLY)
        val annual = FinancialInput(10_000.0, 12.0, 5, CompoundingFrequency.ANNUALLY)
        val r1 = calculateCompoundInterest(base)
        val r2 = calculateCompoundInterest(quarterly)
        val r3 = calculateCompoundInterest(annual)
        assertTrue(r1.finalAmount > r2.finalAmount)
        assertTrue(r2.finalAmount > r3.finalAmount)
        HistoryRepository.save(base, r1)
        HistoryRepository.save(quarterly, r2)
        HistoryRepository.save(annual, r3)
        assertEquals(3, HistoryRepository.load().size)
    }

    @Test
    fun repositoryCapAt50Entries() {
        HistoryRepository.clear()
        repeat(55) { i ->
            val input = FinancialInput((i + 1) * 100.0, 5.0, 1, CompoundingFrequency.ANNUALLY)
            HistoryRepository.save(input, calculateCompoundInterest(input))
        }
        assertTrue(HistoryRepository.load().size <= 50)
    }

    @Test
    fun stateHistoryMatchesRepository() {
        HistoryRepository.clear()
        val state = FinancialCalculatorState()
        state.onPrincipalChange("3000")
        state.onRateChange("4.0")
        state.onYearsChange("6")
        state.calculate(englishStrings)
        state.onPrincipalChange("7000")
        state.onRateChange("8.0")
        state.onYearsChange("12")
        state.calculate(englishStrings)
        assertEquals(HistoryRepository.load().size, state.history.size)
    }
}
