package com.example.task2

import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.safeContentPadding()) {
            val state = remember { FinancialCalculatorState() }
            FinancialCalculatorScreen(state = state)
        }
    }
}
