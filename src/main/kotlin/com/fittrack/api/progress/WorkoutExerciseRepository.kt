package com.fittrack.api.progress

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface WorkoutExerciseRepository :
    JpaRepository<WorkoutExercise, Long> {

    fun findByWorkoutId(workoutId: Long): List<WorkoutExercise>

    @Query(
        value = """
            SELECT we.*
            FROM workout_exercises we
            JOIN workouts w ON we.workout_id = w.id
            WHERE w.user_id = :userId
            ORDER BY we.id DESC
        """,
        nativeQuery = true
    )
    fun findProgressByUserId(
        @Param("userId") userId: Long
    ): List<WorkoutExercise>
}
