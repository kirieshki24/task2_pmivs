package com.example.task2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FinancialCalculatorTest {

    @Test
    fun basicCompoundInterestMonthly() {
        val input = FinancialInput(
            principal = 10_000.0,
            annualRatePercent = 5.0,
            years = 10,
            frequency = CompoundingFrequency.MONTHLY
        )
        val result = calculateCompoundInterest(input)
        // A = 10000 * (1 + 0.05/12)^(12*10) ≈ 16470.09
        assertTrue(result.finalAmount > 16_000.0)
        assertTrue(result.finalAmount < 17_000.0)
        assertTrue(result.totalProfit > 0)
        assertEquals(result.finalAmount - input.principal, result.totalProfit, 0.01)
    }

    @Test
    fun compoundInterestAnnually() {
        val input = FinancialInput(
            principal = 1_000.0,
            annualRatePercent = 10.0,
            years = 1,
            frequency = CompoundingFrequency.ANNUALLY
        )
        val result = calculateCompoundInterest(input)
        // Simple annual: 1000 * 1.10 = 1100
        assertEquals(1_100.0, result.finalAmount, 0.01)
        assertEquals(100.0, result.totalProfit, 0.01)
    }

    @Test
    fun yearlyValuesHaveCorrectCount() {
        val input = FinancialInput(
            principal = 5_000.0,
            annualRatePercent = 7.0,
            years = 5,
            frequency = CompoundingFrequency.QUARTERLY
        )
        val result = calculateCompoundInterest(input)
        // Should have values for years 0, 1, 2, 3, 4, 5 = 6 entries
        assertEquals(6, result.yearlyValues.size)
        assertEquals(input.principal, result.yearlyValues.first(), 0.01)
        assertEquals(result.finalAmount, result.yearlyValues.last(), 0.01)
    }

    @Test
    fun yearlyValuesAreMonotonicallyIncreasing() {
        val input = FinancialInput(
            principal = 1_000.0,
            annualRatePercent = 5.0,
            years = 10,
            frequency = CompoundingFrequency.MONTHLY
        )
        val result = calculateCompoundInterest(input)
        for (i in 1 until result.yearlyValues.size) {
            assertTrue(result.yearlyValues[i] > result.yearlyValues[i - 1])
        }
    }

    @Test
    fun validationPassesForValidInput() {
        val input = FinancialInput(1_000.0, 5.0, 10, CompoundingFrequency.MONTHLY)
        val errors = validateInput(input)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun validationRejectsNegativePrincipal() {
        val input = FinancialInput(-100.0, 5.0, 10, CompoundingFrequency.MONTHLY)
        val errors = validateInput(input)
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("negative") })
    }

    @Test
    fun validationRejectsZeroYears() {
        val input = FinancialInput(1_000.0, 5.0, 0, CompoundingFrequency.ANNUALLY)
        val errors = validateInput(input)
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("Years") || it.contains("greater") })
    }

    @Test
    fun validationRejectsExcessiveRate() {
        val input = FinancialInput(1_000.0, 5000.0, 10, CompoundingFrequency.ANNUALLY)
        val errors = validateInput(input)
        assertFalse(errors.isEmpty())
    }

    @Test
    fun zeroPrincipalGivesZeroResult() {
        val input = FinancialInput(0.0, 10.0, 5, CompoundingFrequency.MONTHLY)
        val result = calculateCompoundInterest(input)
        assertEquals(0.0, result.finalAmount, 0.001)
        assertEquals(0.0, result.totalProfit, 0.001)
    }

    @Test
    fun monthlyCompoundsMoreThanAnnually() {
        val base = FinancialInput(1_000.0, 10.0, 5, CompoundingFrequency.MONTHLY)
        val annual = FinancialInput(1_000.0, 10.0, 5, CompoundingFrequency.ANNUALLY)
        val r1 = calculateCompoundInterest(base)
        val r2 = calculateCompoundInterest(annual)
        assertTrue(r1.finalAmount > r2.finalAmount)
    }

    @Test
    fun historyRepositorySavesAndLoads() {
        HistoryRepository.clear()
        val input = FinancialInput(1_000.0, 5.0, 5, CompoundingFrequency.MONTHLY)
        val result = calculateCompoundInterest(input)
        HistoryRepository.save(input, result)
        val loaded = HistoryRepository.load()
        assertEquals(1, loaded.size)
        assertEquals(input.principal, loaded.first().input.principal, 0.001)
    }

    @Test
    fun historyRepositoryClearsCorrectly() {
        val input = FinancialInput(2_000.0, 3.0, 2, CompoundingFrequency.QUARTERLY)
        val result = calculateCompoundInterest(input)
        HistoryRepository.save(input, result)
        HistoryRepository.clear()
        assertTrue(HistoryRepository.load().isEmpty())
    }

    @Test
    fun formatMoneyRoundsCorrectly() {
        assertEquals("1000.00", 1000.0.formatMoney())
        assertEquals("1234.57", 1234.567.formatMoney())
        assertEquals("0.10", 0.1.formatMoney())
    }
}
