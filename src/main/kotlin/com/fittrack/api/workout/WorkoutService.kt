package com.fittrack.api.workout

import com.fittrack.api.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class WorkoutService(
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun createMyWorkout(
        email: String,
        name: String,
        performedAt: OffsetDateTime?
    ): WorkoutResponse {
        val user = userRepository.findByEmail(email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "User not found")
            
        val workout = Workout(
            name = name.trim(),
            performedAt = performedAt ?: OffsetDateTime.now(),
            user = user
        )
        
        return workoutRepository.save(workout).toResponse()
    }
    @Transactional(readOnly = true)
    fun getMyWorkouts(email: String, pageable: Pageable): Page<WorkoutResponse> {
        return workoutRepository.findByUserEmail(email, pageable).map { it.toResponse() }
    }

    @Transactional
    fun updateMyWorkout(
        id: Long,
        email: String,
        name: String?,
        performedAt: OffsetDateTime?
    ): WorkoutResponse {
        if (name == null && performedAt == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "At least one field is required"
            )
        }

        val workout = workoutRepository.findByIdAndUserEmail(id, email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Workout not found")

        name?.let {
            if (it.isBlank()) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Workout name must not be blank"
                )
            }
            workout.name = it.trim()
        }

        performedAt?.let {
            workout.performedAt = it
        }

        return workout.toResponse()
    }

    @Transactional
    fun deleteMyWorkout(id: Long, email: String) {
        val workout = workoutRepository.findByIdAndUserEmail(id, email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Workout not found")

        workoutRepository.delete(workout)
    }
}