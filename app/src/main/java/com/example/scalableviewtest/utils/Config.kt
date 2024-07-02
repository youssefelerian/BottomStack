package com.example.scalableviewtest.utils

import androidx.annotation.FloatRange
import androidx.annotation.IntRange

data class Config(
    @IntRange(from = 2)
    val space: Int = 90,
    val maxStackCount: Int = 3,
    val initialStackCount: Int = 0,
    @FloatRange(from = 0.0, to = 1.0)
    val secondaryScale: Float = 0f,
    @FloatRange(from = 0.0, to = 1.0)
    val scaleRatio: Float = 0f,
    @FloatRange(from = 1.0, to = 2.0)
    val parallax: Float = 1f
)