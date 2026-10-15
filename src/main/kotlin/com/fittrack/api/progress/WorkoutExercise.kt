package com.fittrack.api.progress

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "workout_exercises")
class WorkoutExercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var workoutId: Long = 0,

    var exerciseId: Long = 0,

    var sets: Long? = null,

    var repetitions: Long? = null,

    var weight: Double? = null,

    var durationSeconds: Long? = null
)
