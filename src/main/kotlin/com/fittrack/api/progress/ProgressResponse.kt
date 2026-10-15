package com.fittrack.api.progress

data class ProgressResponse(
    val id: Long,
    val workoutId: Long,
    val exerciseId: Long,
    val sets: Long,
    val repetitions: Long
)
