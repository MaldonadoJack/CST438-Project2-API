package com.fittrack.api.progress

import com.fittrack.api.exercise.ExerciseRepository
import com.fittrack.api.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ProgressService(
    private val progressRepository: WorkoutExerciseRepository,
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository,
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    fun recordProgress(
        request: ProgressRequest,
        email: String
    ): ProgressResponse {

        // Validate sets and repetitions
        if (request.sets <= 0 || request.repetitions <= 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Sets and repetitions must be positive"
            )
        }

        // Find the authenticated user
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
            )

        val userId = requireNotNull(user.id)

        // Check that the workout belongs to this user
        val ownsWorkout = jdbcTemplate.queryForObject(
            """
            SELECT EXISTS (
                SELECT 1 FROM workouts
                WHERE id = ? AND user_id = ?
            )
            """.trimIndent(),
            Boolean::class.java,
            request.workoutId,
            userId
        ) ?: false

        if (!ownsWorkout) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Workout not found"
            )
        }

        // Check that the exercise exists
        if (!exerciseRepository.existsById(request.exerciseId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Exercise not found"
            )
        }

        // Save progress to the database
        val progress = progressRepository.save(
            WorkoutExercise(
                workoutId = request.workoutId,
                exerciseId = request.exerciseId,
                sets = request.sets,
                repetitions = request.repetitions
            )
        )

        return ProgressResponse(
            id = requireNotNull(progress.id),
            workoutId = progress.workoutId,
            exerciseId = progress.exerciseId,
            sets = requireNotNull(progress.sets),
            repetitions = requireNotNull(progress.repetitions)
        )
    }

    @Transactional(readOnly = true)
    fun getMyProgress(email: String): List<ProgressResponse> {

        // Find the authenticated user
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
            )

        val userId = requireNotNull(user.id)

        // Retrieve progress belonging to this user
        return progressRepository.findProgressByUserId(userId).map { progress ->
            ProgressResponse(
                id = requireNotNull(progress.id),
                workoutId = progress.workoutId,
                exerciseId = progress.exerciseId,
                sets = requireNotNull(progress.sets),
                repetitions = requireNotNull(progress.repetitions)
            )
        }
    }
}
