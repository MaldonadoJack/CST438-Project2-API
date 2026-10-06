package com.fittrack.api.exercise

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Repository
class ExerciseRepository(private val jdbc: JdbcTemplate) {
    fun insert(request: ExerciseCreateRequest): ExerciseResponse {
        val keys = GeneratedKeyHolder()
        val name = request.name.trim()
        jdbc.update({ connection ->
            connection.prepareStatement(
                "INSERT INTO exercises (name, description, category) VALUES (?, ?, ?)",
                arrayOf("id")
            ).apply {
                setString(1, name)
                setString(2, request.description)
                setString(3, request.category)
            }
        }, keys)
        return ExerciseResponse(requireNotNull(keys.key).toLong(), name, request.description, request.category)
    }
}

@Service
class ExerciseService(private val repository: ExerciseRepository) {
    @Transactional
    fun create(request: ExerciseCreateRequest): ExerciseResponse = repository.insert(request)
}
