package com.fittrack.api.workout

import java.time.OffsetDateTime

data class WorkoutResponse(
    val id: Long,
    val name: String,
    val performedAt: OffsetDateTime?,
    val isCompleted: Boolean,
    val completedAt: OffsetDateTime?
)

fun Workout.toResponse() = WorkoutResponse(
    id = id ?: 0,
    name = name,
    performedAt = performedAt,
    isCompleted = isCompleted,
    completedAt = completedAt
)
