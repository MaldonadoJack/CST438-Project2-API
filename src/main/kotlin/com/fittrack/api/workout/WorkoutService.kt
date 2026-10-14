package com.fittrack.api.workout

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkoutService(
    private val workoutRepository: WorkoutRepository
) {
    @Transactional(readOnly = true)
    fun getMyWorkouts(email: String, pageable: Pageable): Page<WorkoutResponse> {
        return workoutRepository.findByUserEmail(email, pageable).map { it.toResponse() }
    }
}
