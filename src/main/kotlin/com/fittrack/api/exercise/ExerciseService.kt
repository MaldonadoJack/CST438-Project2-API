package com.fittrack.api.exercise

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.EntityNotFoundException

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val exerciseWriteRepository: ExerciseWriteRepository
) {
    fun searchExercises(
        search: String?,
        pageable: Pageable
    ): Page<Exercise> {
        return if (search.isNullOrBlank()) {
            exerciseRepository.findAll(pageable)
        } else {
            exerciseRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                search,
                search,
                pageable
            )
        }
    }

    @Transactional
    fun create(request: ExerciseCreateRequest): ExerciseResponse =
        exerciseWriteRepository.insert(request)

    @Transactional
    fun replace(request: ExerciseReplaceRequest): ExerciseResponse {
        if (!exerciseRepository.existsById(request.id)) {
            throw EntityNotFoundException("Exercise with id ${request.id} was not found")
        }

        val name = request.name.trim()
        exerciseWriteRepository.replace(request, name)

        return ExerciseResponse(
            id = request.id,
            name = name,
            description = request.description,
            category = request.category
        )
    }

    @Transactional
    fun delete(id: Long) {
        if (!exerciseRepository.existsById(id)) {
            throw EntityNotFoundException("Exercise with id $id was not found")
        }

        exerciseWriteRepository.delete(id)
    }

    @Transactional
    fun updateDescription(
        id: Long,
        description: String?
    ): ExerciseResponse {
        val exercise = exerciseRepository.findById(id)
            .orElseThrow {
                EntityNotFoundException("Exercise with id $id was not found")
            }

        exerciseWriteRepository.updateDescription(id, description)

        return ExerciseResponse(
            id = requireNotNull(exercise.id),
            name = exercise.name,
            description = description,
            category = exercise.category
        )
    }
}
