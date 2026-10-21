package com.fittrack.api.workout

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

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

    @PostMapping
    fun createWorkout(
        @Valid @RequestBody request: WorkoutCreateRequest,
        authentication: Authentication
    ): ResponseEntity<WorkoutResponse> {
        val response = workoutService.createMyWorkout(
            authentication.name,
            request.name,
            request.performedAt
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PatchMapping("/{workoutId}")
    fun updateWorkout(
        @PathVariable workoutId: Long,
        @RequestBody request: WorkoutPatchRequest,
        authentication: Authentication
    ): WorkoutResponse {
        return workoutService.updateMyWorkout(
            workoutId,
            authentication.name,
            request.name,
            request.performedAt
        )
    }

    @DeleteMapping("/{workoutId}")
    fun deleteWorkout(
        @PathVariable workoutId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        workoutService.deleteMyWorkout(workoutId, authentication.name)
        return ResponseEntity.noContent().build()
    }
}

data class WorkoutCreateRequest (
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
    val performedAt: OffsetDateTime? = null
)

data class WorkoutPatchRequest (
    val name: String? = null,
    val performedAt: OffsetDateTime? = null
)