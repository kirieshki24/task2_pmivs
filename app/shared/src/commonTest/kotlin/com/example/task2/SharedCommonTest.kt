package com.example.task2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

// UI logic / widget-level tests (no Compose test runner needed — tests logic used by UI)
class FinancialCalculatorStateTest {

    private fun freshState() = FinancialCalculatorState()

    @Test
    fun initialStateHasNoResult() {
        val state = freshState()
        assertNull(state.result)
        assertNull(state.errorMessage)
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun calculateWithValidInputProducesResult() {
        val state = freshState()
        state.onPrincipalChange("10000")
        state.onRateChange("5.0")
        state.onYearsChange("10")
        state.onFrequencyChange(CompoundingFrequency.MONTHLY)
        state.calculate(englishStrings)
        assertNotNull(state.result)
        assertNull(state.errorMessage)
        assertTrue(state.result!!.finalAmount > 10_000.0)
    }

    @Test
    fun calculateWithInvalidPrincipalSetsError() {
        val state = freshState()
        state.onPrincipalChange("abc")
        state.calculate(englishStrings)
        assertNull(state.result)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun calculateWithEmptyFieldSetsError() {
        val state = freshState()
        state.onPrincipalChange("")
        state.calculate(englishStrings)
        assertNull(state.result)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun calculateWithNegativePrincipalSetsError() {
        val state = freshState()
        state.onPrincipalChange("-500")
        state.onRateChange("5")
        state.onYearsChange("5")
        state.calculate(englishStrings)
        assertNull(state.result)
        assertNotNull(state.errorMessage)
        assertEquals(englishStrings.errorNegativePrincipal, state.errorMessage)
    }

    @Test
    fun calculateWithZeroYearsSetsError() {
        val state = freshState()
        state.onPrincipalChange("1000")
        state.onRateChange("5")
        state.onYearsChange("0")
        state.calculate(englishStrings)
        assertNull(state.result)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun calculateSavesToHistory() {
        HistoryRepository.clear()
        val state = freshState()
        state.onPrincipalChange("5000")
        state.onRateChange("3.5")
        state.onYearsChange("7")
        state.calculate(englishStrings)
        assertNotNull(state.result)
        assertTrue(state.history.isNotEmpty())
        assertEquals(5000.0, state.history.first().input.principal, 0.01)
    }

    @Test
    fun clearHistoryEmptiesList() {
        HistoryRepository.clear()
        val state = freshState()
        state.onPrincipalChange("1000")
        state.onRateChange("5")
        state.onYearsChange("3")
        state.calculate(englishStrings)
        assertTrue(state.history.isNotEmpty())
        state.clearHistory()
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun languageSwitchChangesStrings() {
        val state = freshState()
        assertEquals(Language.ENGLISH, state.language)
        state.onLanguageChange(Language.RUSSIAN)
        assertEquals(Language.RUSSIAN, state.language)
        val strings = getStrings(state.language)
        assertEquals(russianStrings.calculate, strings.calculate)
    }

    @Test
    fun frequencyChangeIsReflected() {
        val state = freshState()
        assertEquals(CompoundingFrequency.MONTHLY, state.frequency)
        state.onFrequencyChange(CompoundingFrequency.QUARTERLY)
        assertEquals(CompoundingFrequency.QUARTERLY, state.frequency)
    }

    @Test
    fun errorClearedWhenInputChanges() {
        val state = freshState()
        state.onPrincipalChange("bad")
        state.calculate(englishStrings)
        assertNotNull(state.errorMessage)
        state.onPrincipalChange("1000")
        assertNull(state.errorMessage)
    }
}

class StringLocalizationTest {

    @Test
    fun allLanguagesReturnNonEmptyStrings() {
        Language.entries.forEach { lang ->
            val strings = getStrings(lang)
            assertTrue(strings.appName.isNotEmpty(), "appName empty for $lang")
            assertTrue(strings.calculate.isNotEmpty(), "calculate empty for $lang")
            assertTrue(strings.monthly.isNotEmpty(), "monthly empty for $lang")
        }
    }

    @Test
    fun languagesHaveDifferentCalculateLabels() {
        val en = getStrings(Language.ENGLISH).calculate
        val ru = getStrings(Language.RUSSIAN).calculate
        val be = getStrings(Language.BELARUSIAN).calculate
        assertFalse(en == ru)
        assertFalse(ru == be)
    }
}
