package com.fittrack.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import javax.sql.DataSource

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgressControllerIntegrationTest {

    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var jdbc: JdbcTemplate
    @Autowired lateinit var dataSource: DataSource

    private var workoutId = 0L
    private var otherWorkoutId = 0L
    private var exerciseId = 0L

    private val endpoint = "/api/v1/fittrack/progress"

    @BeforeEach
    fun setup() {
        check(dataSource.connection.use {
            it.metaData.url.startsWith("jdbc:h2:")
        }) { "Refusing to reset a non-H2 database" }

        jdbc.execute("DROP ALL OBJECTS")
        jdbc.execute(
            "CREATE DOMAIN TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE"
        )
        ResourceDatabasePopulator(
            FileSystemResource("database/schema.sql")
        ).execute(dataSource)

        jdbc.update(
            """
            INSERT INTO users (email, display_name, role)
            VALUES (?, ?, ?)
            """.trimIndent(),
            "student@example.com", "Student", "user"
        )

        jdbc.update(
            """
            INSERT INTO users (email, display_name, role)
            VALUES (?, ?, ?)
            """.trimIndent(),
            "other@example.com", "Other", "user"
        )

        val userId = jdbc.queryForObject(
            "SELECT id FROM users WHERE email = ?",
            Long::class.java,
            "student@example.com"
        )!!

        val otherUserId = jdbc.queryForObject(
            "SELECT id FROM users WHERE email = ?",
            Long::class.java,
            "other@example.com"
        )!!

        jdbc.update(
            "INSERT INTO exercises (name, category) VALUES (?, ?)",
            "Squat", "strength"
        )

        exerciseId = jdbc.queryForObject(
            "SELECT id FROM exercises WHERE name = ?",
            Long::class.java,
            "Squat"
        )!!

        jdbc.update(
            "INSERT INTO workouts (name, user_id) VALUES (?, ?)",
            "Leg Day", userId
        )

        jdbc.update(
            "INSERT INTO workouts (name, user_id) VALUES (?, ?)",
            "Other Workout", otherUserId
        )

        workoutId = jdbc.queryForObject(
            "SELECT id FROM workouts WHERE name = ?",
            Long::class.java,
            "Leg Day"
        )!!

        otherWorkoutId = jdbc.queryForObject(
            "SELECT id FROM workouts WHERE name = ?",
            Long::class.java,
            "Other Workout"
        )!!
    }

    private fun studentJwt() = jwt().jwt {
        it.subject("student123")
        it.claim("email", "student@example.com")
    }

    private fun progressJson(
        workout: Long = workoutId,
        exercise: Long = exerciseId,
        sets: Long = 3,
        repetitions: Long = 12
    ) = """
        {
          "workoutId": $workout,
          "exerciseId": $exercise,
          "sets": $sets,
          "repetitions": $repetitions
        }
    """.trimIndent()

    @Test
    fun recordsSetsAndReps() {
        mvc.perform(
            post(endpoint)
                .with(studentJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(progressJson())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.sets").value(3))
            .andExpect(jsonPath("$.repetitions").value(12))

        val count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM workout_exercises",
            Int::class.java
        )
        org.junit.jupiter.api.Assertions.assertEquals(1, count)
    }

    @Test
    fun rejectsZeroSets() {
        mvc.perform(
            post(endpoint)
                .with(studentJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(progressJson(sets = 0))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun rejectsNegativeRepetitions() {
        mvc.perform(
            post(endpoint)
                .with(studentJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(progressJson(repetitions = -1))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun rejectsUnknownExercise() {
        mvc.perform(
            post(endpoint)
                .with(studentJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(progressJson(exercise = 99999))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun rejectsAnotherUsersWorkout() {
        mvc.perform(
            post(endpoint)
                .with(studentJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(progressJson(workout = otherWorkoutId))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun retrievesOnlyMyProgress() {
        jdbc.update(
            """
            INSERT INTO workout_exercises
                (workout_id, exercise_id, sets, repetitions)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            workoutId, exerciseId, 3, 12
        )

        jdbc.update(
            """
            INSERT INTO workout_exercises
                (workout_id, exercise_id, sets, repetitions)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            otherWorkoutId, exerciseId, 5, 20
        )

        mvc.perform(get(endpoint).with(studentJwt()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].sets").value(3))
            .andExpect(jsonPath("$[0].repetitions").value(12))
    }

    @Test
    fun anonymousCannotRecordProgress() {
        mvc.perform(
            post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(progressJson())
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun anonymousCannotViewProgress() {
        mvc.perform(get(endpoint))
            .andExpect(status().isUnauthorized)
    }
}
