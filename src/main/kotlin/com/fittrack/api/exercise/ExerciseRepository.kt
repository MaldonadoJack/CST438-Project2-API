package com.fittrack.api.exercise

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ExerciseRepository : JpaRepository<Exercise, Long> {
    fun findByNameContainingIgnoreCaseOrMuscleGroupContainingIgnoreCase(
        name: String,
        muscleGroup: String,
        pageable: Pageable
    ): Page<Exercise>
}