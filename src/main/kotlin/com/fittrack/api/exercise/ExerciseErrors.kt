package com.fittrack.api.exercise

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExerciseErrors {
    @ExceptionHandler(DuplicateKeyException::class)
    fun duplicateName(): ProblemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, "An exercise with this name already exists."
    )
}
