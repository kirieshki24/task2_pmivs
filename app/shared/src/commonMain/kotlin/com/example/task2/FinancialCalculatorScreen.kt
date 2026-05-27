package com.example.task2

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FinancialCalculatorScreen(
    state: FinancialCalculatorState = remember { FinancialCalculatorState() }
) {
    val strings = getStrings(state.language)
    val config = PlatformConfig
    val scrollState = rememberScrollState()

    LaunchedEffect(state.result) {
        if (state.result != null) {
            kotlinx.coroutines.delay(350)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        AppHeader(strings = strings, state = state)

        // Input card
        InputCard(strings = strings, state = state, config = config)

        // Error
        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            state.errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Calculate button
        CalculateButton(strings = strings, config = config, onClick = { state.calculate(strings) })

        // Results
        AnimatedVisibility(
            visible = state.result != null,
            enter = fadeIn(tween(600)) + expandVertically(tween(600)),
            exit = fadeOut() + shrinkVertically()
        ) {
            state.result?.let { result ->
                ResultsCard(strings = strings, result = result, state = state)
            }
        }

        // History
        AnimatedVisibility(
            visible = state.history.isNotEmpty(),
            enter = fadeIn(tween(400)) + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            HistoryCard(strings = strings, state = state)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun AppHeader(strings: AppStrings, state: FinancialCalculatorState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = strings.appName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        LanguageSelector(state = state)
    }
}

@Composable
private fun LanguageSelector(state: FinancialCalculatorState) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(state.language.code.uppercase(), fontSize = 12.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Language.entries.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.displayName) },
                    onClick = {
                        state.onLanguageChange(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun InputCard(
    strings: AppStrings,
    state: FinancialCalculatorState,
    config: PlatformConfig
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (config.showRateSlider) {
                // Android: Principal + Years в одну строку, Rate со слайдером отдельно
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberInputField(
                        label = strings.initialAmount,
                        value = state.principalText,
                        onValueChange = state::onPrincipalChange,
                        placeholder = strings.enterAmount,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal
                    )
                    NumberInputField(
                        label = strings.termYears,
                        value = state.yearsText,
                        onValueChange = state::onYearsChange,
                        placeholder = strings.enterYears,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                }
                RateWithSlider(strings = strings, state = state)
            } else {
                // Остальные платформы: Principal + Rate в одну строку, Years отдельно
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberInputField(
                        label = strings.initialAmount,
                        value = state.principalText,
                        onValueChange = state::onPrincipalChange,
                        placeholder = strings.enterAmount,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal
                    )
                    NumberInputField(
                        label = strings.annualRate,
                        value = state.rateText,
                        onValueChange = state::onRateChange,
                        placeholder = strings.enterRate,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal
                    )
                }
                NumberInputField(
                    label = strings.termYears,
                    value = state.yearsText,
                    onValueChange = state::onYearsChange,
                    placeholder = strings.enterYears,
                    modifier = if (config.useFixedInputWidth) Modifier.width(140.dp) else Modifier.fillMaxWidth(0.5f),
                    keyboardType = KeyboardType.Number
                )
            }

            // Compounding
            Text(
                text = strings.compounding,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CompoundingSelector(
                selectedFrequency = state.frequency,
                onFrequencySelected = state::onFrequencyChange,
                strings = strings,
                useFlatStyle = config.useFlatButtons
            )
        }
    }
}

@Composable
private fun NumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
    )
}

@Composable
private fun RateWithSlider(strings: AppStrings, state: FinancialCalculatorState) {
    val sliderValue = state.rateText.toFloatOrNull() ?: 5f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.annualRate,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${state.rateText}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = sliderValue.coerceIn(0f, 50f),
            onValueChange = { state.onRateChange(((it * 10).toLong() / 10.0).toString()) },
            valueRange = 0f..50f,
            steps = 499,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.rateText,
            onValueChange = state::onRateChange,
            label = { Text(strings.rate) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
    }
}

@Composable
private fun CompoundingSelector(
    selectedFrequency: CompoundingFrequency,
    onFrequencySelected: (CompoundingFrequency) -> Unit,
    strings: AppStrings,
    useFlatStyle: Boolean
) {
    val options = listOf(
        CompoundingFrequency.MONTHLY to strings.monthly,
        CompoundingFrequency.QUARTERLY to strings.quarterly,
        CompoundingFrequency.ANNUALLY to strings.annually
    )

    if (useFlatStyle) {
        // iOS-style flat segmented control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            options.forEach { (freq, label) ->
                val isSelected = freq == selectedFrequency
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onFrequencySelected(freq) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    } else {
        // Material 3 segmented buttons
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (freq, label) ->
                SegmentedButton(
                    selected = freq == selectedFrequency,
                    onClick = { onFrequencySelected(freq) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
        }
    }
}

@Composable
private fun CalculateButton(
    strings: AppStrings,
    config: PlatformConfig,
    onClick: () -> Unit
) {
    if (config.useFlatButtons) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(strings.calculate, fontWeight = FontWeight.Medium)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(strings.calculate, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ResultsCard(
    strings: AppStrings,
    result: FinancialResult,
    state: FinancialCalculatorState
) {
    val config = PlatformConfig
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = strings.results,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            ResultRow(
                label = strings.finalAmount,
                value = "${getStrings(state.language).currency}${result.finalAmount.formatMoney()}",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            ResultRow(
                label = strings.totalProfit,
                value = "+${getStrings(state.language).currency}${result.totalProfit.formatMoney()}",
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = strings.capitalGrowth,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                GrowthChart(
                    values = result.yearlyValues,
                    yearLabel = strings.year,
                    amountLabel = strings.amount,
                    currency = getStrings(state.language).currency,
                    modifier = Modifier.fillMaxSize(),
                    showGradient = config.chartShowGradient,
                    lineWidthDp = config.chartLineWidthDp,
                    interactive = config.chartInteractive
                )
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = color, fontSize = 14.sp)
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun HistoryCard(strings: AppStrings, state: FinancialCalculatorState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.history,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = state::clearHistory) {
                    Text(strings.clearHistory, fontSize = 12.sp)
                }
            }
            HorizontalDivider()
            state.history.take(5).forEach { entry ->
                HistoryItem(entry = entry, currency = strings.currency)
            }
        }
    }
}

@Composable
private fun HistoryItem(entry: HistoryEntry, currency: String) {
    val freq = when (entry.input.frequency) {
        CompoundingFrequency.MONTHLY -> "M"
        CompoundingFrequency.QUARTERLY -> "Q"
        CompoundingFrequency.ANNUALLY -> "A"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$currency${entry.input.principal.formatMoney()} · ${entry.input.annualRatePercent}% · ${entry.input.years}y · $freq",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$currency${entry.result.finalAmount.formatMoney()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
