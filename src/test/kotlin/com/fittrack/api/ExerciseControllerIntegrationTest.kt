
package com.fittrack.api

import com.fittrack.api.exercise.Exercise
import com.fittrack.api.exercise.ExerciseRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExerciseControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val exerciseRepository: ExerciseRepository
) {
    @BeforeEach
    fun setUp() {
        exerciseRepository.deleteAll()

        exerciseRepository.saveAll(
            listOf(
                Exercise(
                    name = "Push-Up",
                    category = "Chest",
                    description = "A bodyweight pressing exercise"
                ),
                Exercise(
                    name = "Push Press",
                    category = "Shoulders",
                    description = "An overhead pressing exercise"
                ),
                Exercise(
                    name = "Squat",
                    category = "Legs",
                    description = "A lower-body exercise"
                )
            )
        )
    }

    @Test
    fun `searches and paginates exercises`() {
        mockMvc.perform(
            get("/fittrack/exercises")
                .param("search", "push")
                .param("page", "0")
                .param("size", "1")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(1)))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.number").value(0))
    }

    @Test
    fun `returns all exercises when no search is provided`() {
        mockMvc.perform(
            get("/fittrack/exercises")
                .param("page", "0")
                .param("size", "2")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(2)))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
    }

    @Test
    fun `returns the second page of exercises`() {
        mockMvc.perform(
            get("/fittrack/exercises")
                .param("page", "1")
                .param("size", "2")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(1)))
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.last").value(true))
    }
}
