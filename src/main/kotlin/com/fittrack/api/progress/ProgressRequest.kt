package com.fittrack.api.progress

data class ProgressRequest(
    val workoutId: Long,
    val exerciseId: Long,
    val sets: Long,
    val repetitions: Long
)
