package com.fittrack.api.exercise

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class ExerciseCreateRequest(
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
    @GetMapping("/fittrack/exercises")
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
}
