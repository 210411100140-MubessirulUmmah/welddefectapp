package com.example.welddefectdetector

data class DetectionResponse(
    val detections: List<Detection>
)

data class Detection(
    val bbox: List<Float>, // [x_min, y_min, x_max, y_max]
    val `class`: String,
    val score: Float
)