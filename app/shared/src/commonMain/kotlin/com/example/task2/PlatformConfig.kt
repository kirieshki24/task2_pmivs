package com.example.task2

expect object PlatformConfig {
    val showRateSlider: Boolean
    val chartShowGradient: Boolean
    val chartLineWidthDp: Float
    val useFixedInputWidth: Boolean
    val useFlatButtons: Boolean
    val chartInteractive: Boolean
    val platformName: String
}
