package com.fittrack.api

import com.fittrack.api.user.User
import com.fittrack.api.user.UserRepository
import com.fittrack.api.workout.Workout
import com.fittrack.api.workout.WorkoutRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch

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
        val workout = workoutRepository.findAll().first { it.name == "Morning Run" }

        mockMvc.perform(
            patch("/api/v1/fittrack/workouts/${workout.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Updated Morning Run"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(workout.id))
            .andExpect(jsonPath("$.name").value("Updated Morning Run"))

        assertTrue(
            workoutRepository.findById(workout.id!!).get().name == "Updated Morning Run"
        )
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `cannot patch another user's workout`() {
        val workout = workoutRepository.findAll().first { it.name == "Yoga" }

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
        val workout = workoutRepository.findAll().first { it.name == "Evening Lift" }

        mockMvc.perform(
            delete("/api/v1/fittrack/workouts/${workout.id}")
        )
            .andExpect(status().isNoContent)

        assertFalse(workoutRepository.findById(workout.id!!).isPresent)
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    fun `cannot delete another user's workout`() {
        val workout = workoutRepository.findAll().first { it.name == "Yoga" }

        mockMvc.perform(
            delete("/api/v1/fittrack/workouts/${workout.id}")
        )
            .andExpect(status().isNotFound)

        assertTrue(workoutRepository.findById(workout.id!!).isPresent)
    }
}
