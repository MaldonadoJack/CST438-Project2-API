package com.fittrack.api.exercise

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fittrack/exercises")
class ExerciseController(
    private val exerciseService: ExerciseService
) {
    @GetMapping
    fun searchExercises(
        @RequestParam(required = false) search: String?,
        @PageableDefault(
            size = 10,
            sort = ["name"],
            direction = Sort.Direction.ASC
        )
        pageable: Pageable
    ) = exerciseService.searchExercises(search, pageable)
}