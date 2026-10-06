package com.fittrack.api.exercise

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository
) {
    fun searchExercises(
        search: String?,
        pageable: Pageable
    ): Page<Exercise> {
        return if (search.isNullOrBlank()) {
            exerciseRepository.findAll(pageable)
        } else {
            exerciseRepository.findByNameContainingIgnoreCaseOrMuscleGroupContainingIgnoreCase(search, search, pageable)
        }
    }
}