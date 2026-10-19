package com.fittrack.api.exercise

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable;

data class ExerciseCreateRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
    @field:Size(max = 1000)
    val description: String? = null,
    @field:Size(max = 100)
    val category: String? = null
)

data class ExerciseDescriptionPatchRequest(
    @field:Size(max = 1000)
    val description: String?
)

data class ExerciseReplaceRequest(
    @field:Positive
    val id: Long,
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
    @field:Size(max = 1000)
    val description: String? = null,
    @field:Size(max = 100)
    val category: String? = null
)

data class ExerciseResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val category: String?
)

@RestController
@RequestMapping
class ExerciseController(
    private val exerciseService: ExerciseService
) {
    @GetMapping("/api/v1/fittrack/exercises")
    fun searchExercises(
        @RequestParam(required = false) search: String?,
        @PageableDefault(
            size = 10,
            sort = ["name"],
            direction = Sort.Direction.ASC
        )
        pageable: Pageable
    ) = exerciseService.searchExercises(search, pageable)

    @PostMapping("/api/v1/fittrack/exercises")
    fun createExercise(
        @Valid @RequestBody request: ExerciseCreateRequest
    ): ResponseEntity<ExerciseResponse> =
        ResponseEntity.status(201).body(exerciseService.create(request))

    @PutMapping("/api/v1/fittrack/exercises")
    fun replaceExercise(
        @Valid @RequestBody request: ExerciseReplaceRequest
    ): ExerciseResponse = exerciseService.replace(request)

    @DeleteMapping("/api/v1/fittrack/exercises")
    fun deleteExercise(
        @RequestParam @Positive id: Long
    ): ResponseEntity<Void> {
        exerciseService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/api/v1/fittrack/exercises/{id}")
    fun updateDescription(
        @PathVariable id: Long,
        @Valid @RequestBody request: ExerciseDescriptionPatchRequest
    ): ExerciseResponse =
        exerciseService.updateDescription(id, request.description)
}
