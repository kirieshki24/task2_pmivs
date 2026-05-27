package com.example.task2

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform