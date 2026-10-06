package com.fittrack.api.exercise

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class ExerciseCreateRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:Size(max = 1000) val description: String? = null,
    @field:Size(max = 100) val category: String? = null
)

data class ExerciseResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val category: String?
)

@RestController
@RequestMapping("/api/v1/fittrack/exercises")
class ExerciseController(private val service: ExerciseService) {
    @PostMapping
    fun create(@Valid @RequestBody request: ExerciseCreateRequest): ResponseEntity<ExerciseResponse> =
        ResponseEntity.status(201).body(service.create(request))
}
