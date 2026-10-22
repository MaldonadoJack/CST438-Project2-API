package com.fittrack.api

import com.fittrack.api.user.User
import com.fittrack.api.user.UserRepository
import com.fittrack.api.workout.Workout
import com.fittrack.api.workout.WorkoutRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkoutControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository
) {

    @BeforeEach
    fun setUp() {
        workoutRepository.deleteAll()
        userRepository.deleteAll()

        val testUser1 = userRepository.save(
            User(
                email = "user1@example.com",
                displayName = "Test User 1",
                role = "user"
            )
        )

        val testUser2 = userRepository.save(
            User(
                email = "user2@example.com",
                displayName = "Test User 2",
                role = "user"
            )
        )

        workoutRepository.saveAll(
            listOf(
                Workout(
                    name = "Morning Run",
                    performedAt = OffsetDateTime.now().minusDays(1),
                    user = testUser1
                ),
                Workout(
                    name = "Evening Lift",
                    performedAt = OffsetDateTime.now().minusHours(2),
                    user = testUser1
                ),
                Workout(
                    name = "Yoga",
                    performedAt = OffsetDateTime.now().minusDays(2),
                    user = testUser2
                )
            )
        )
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `returns only the authenticated user's workouts`() {
        mockMvc.perform(
            get("/api/v1/fittrack/workouts")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(2)))
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    @WithMockUser(username = "user2@example.com")
    fun `returns another user's workouts`() {
        mockMvc.perform(
            get("/api/v1/fittrack/workouts")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(1)))
            .andExpect(jsonPath("$.content[0].name").value("Yoga"))
    }

    @Test
    fun `fails with unauthorized when no user is authenticated`() {
        mockMvc.perform(
            get("/api/v1/fittrack/workouts")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `patches a workout owned by the authenticated user`() {
        val workout = workoutRepository.findAll()
            .first { it.name == "Morning Run" }

        mockMvc.perform(
            patch("/api/v1/fittrack/workouts/${workout.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Updated Morning Run"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(workout.id))
            .andExpect(jsonPath("$.name").value("Updated Morning Run"))

        assertTrue(
            workoutRepository.findById(workout.id!!).get().name ==
                "Updated Morning Run"
        )
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `cannot patch another user's workout`() {
        val workout = workoutRepository.findAll()
            .first { it.name == "Yoga" }

        mockMvc.perform(
            patch("/api/v1/fittrack/workouts/${workout.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Should Not Change"}""")
        )
            .andExpect(status().isNotFound)

        assertTrue(
            workoutRepository.findById(workout.id!!).get().name == "Yoga"
        )
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `deletes a workout owned by the authenticated user`() {
        val workout = workoutRepository.findAll()
            .first { it.name == "Evening Lift" }

        mockMvc.perform(
            delete("/api/v1/fittrack/workouts/${workout.id}")
        )
            .andExpect(status().isNoContent)

        assertFalse(workoutRepository.findById(workout.id!!).isPresent)
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `cannot delete another user's workout`() {
        val workout = workoutRepository.findAll()
            .first { it.name == "Yoga" }

        mockMvc.perform(
            delete("/api/v1/fittrack/workouts/${workout.id}")
        )
            .andExpect(status().isNotFound)

        assertTrue(workoutRepository.findById(workout.id!!).isPresent)
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `creates a new workout successfully`() {
        mockMvc.perform(
            post("/api/v1/fittrack/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "New Workout",
                        "performedAt": "2023-10-10T10:00:00Z"
                    }
                    """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("New Workout"))
            .andExpect(
                jsonPath("$.performedAt")
                    .value("2023-10-10T10:00:00Z")
            )
            .andExpect(jsonPath("$.id").exists())
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `fails to create a workout with empty name`() {
        mockMvc.perform(
            post("/api/v1/fittrack/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": ""
                    }
                    """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    // Issue #20: Mark a Workout as Completed

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `completes a workout owned by the authenticated user`() {
        val workout = workoutRepository.findAll()
            .first { it.name == "Morning Run" }

        mockMvc.perform(
            patch("/api/v1/fittrack/workouts/${workout.id}/complete")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(workout.id))
            .andExpect(jsonPath("$.isCompleted").value(true))
            .andExpect(jsonPath("$.completedAt").isNotEmpty)

        val updated = workoutRepository.findById(workout.id!!).get()

        assertTrue(updated.isCompleted)
        assertNotNull(updated.completedAt)
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `completing a workout twice preserves completion date`() {
        val workout = workoutRepository.findAll()
            .first { it.name == "Morning Run" }

        val url = "/api/v1/fittrack/workouts/${workout.id}/complete"

        mockMvc.perform(patch(url))
            .andExpect(status().isOk)

        val firstCompletion = workoutRepository
            .findById(workout.id!!)
            .get()
            .completedAt

        mockMvc.perform(patch(url))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isCompleted").value(true))

        val secondCompletion = workoutRepository
            .findById(workout.id!!)
            .get()
            .completedAt

        assertNotNull(firstCompletion)
        assertEquals(firstCompletion, secondCompletion)
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `cannot complete another user's workout`() {
        val workout = workoutRepository.findAll()
            .first { it.name == "Yoga" }

        mockMvc.perform(
            patch("/api/v1/fittrack/workouts/${workout.id}/complete")
        )
            .andExpect(status().isNotFound)

        val unchanged = workoutRepository.findById(workout.id!!).get()

        assertFalse(unchanged.isCompleted)
        assertTrue(unchanged.completedAt == null)
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `cannot complete a nonexistent workout`() {
        mockMvc.perform(
            patch("/api/v1/fittrack/workouts/999999/complete")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `cannot complete a workout without authentication`() {
        val workout = workoutRepository.findAll()
            .first { it.name == "Morning Run" }

        mockMvc.perform(
            patch("/api/v1/fittrack/workouts/${workout.id}/complete")
        )
            .andExpect(status().isUnauthorized)

        val unchanged = workoutRepository.findById(workout.id!!).get()

        assertFalse(unchanged.isCompleted)
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `new workouts are initially incomplete`() {
        mockMvc.perform(
            post("/api/v1/fittrack/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"New Incomplete Workout"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.isCompleted").value(false))
            .andExpect(jsonPath("$.completedAt").value(null))
    }
}
