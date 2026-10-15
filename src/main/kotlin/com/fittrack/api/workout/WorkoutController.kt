package com.fittrack.api.workout

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/fittrack/workouts")
class WorkoutController(
    private val workoutService: WorkoutService
) {
    @GetMapping
    fun getMyWorkouts(
        authentication: Authentication,
        @PageableDefault(
            size = 20,
            sort = ["performedAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable
    ): Page<WorkoutResponse> {
        val email = authentication.name
        if (email.isBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated email is required")
        }
        
        return workoutService.getMyWorkouts(email, pageable)
    }
}
