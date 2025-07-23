package com.example.welddefectdetector

import android.graphics.Bitmap

data class DetectionResult(
    val bitmap: Bitmap,
    val labels: String
)
