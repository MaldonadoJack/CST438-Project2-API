package com.fittrack.api

import com.fittrack.api.config.SecurityConfig
import com.fittrack.api.exercise.ExerciseController
import com.fittrack.api.exercise.ExerciseResponse
import com.fittrack.api.exercise.ExerciseService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ExerciseController::class)
@Import(SecurityConfig::class)
class ExerciseControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    @MockitoBean
    private lateinit var exerciseService: ExerciseService

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin can update exercise description`() {
        val response = ExerciseResponse(
            id = 1L,
            name = "Squat",
            description = "Updated squat description.",
            category = "Legs"
        )

        `when`(
            exerciseService.updateDescription(
                1L,
                "Updated squat description."
            )
        ).thenReturn(response)

        mockMvc.perform(
            patch("/api/v1/fittrack/exercises/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "description": "Updated squat description."
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Squat"))
            .andExpect(jsonPath("$.description").value("Updated squat description."))
            .andExpect(jsonPath("$.category").value("Legs"))

        verify(exerciseService).updateDescription(
            1L,
            "Updated squat description."
        )
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `regular user cannot update exercise description`() {
        mockMvc.perform(
            patch("/api/v1/fittrack/exercises/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "description": "Unauthorized update."
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `unauthenticated user cannot update exercise description`() {
        mockMvc.perform(patch("/api/v1/fittrack/exercises/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                      "description": "Unauthorized update."
                    }
                    """.trimIndent()
            )
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `description cannot exceed 1000 characters`() {
        val tooLongDescription = "a".repeat(1001)

        mockMvc.perform(
            patch("/api/v1/fittrack/exercises/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "description": "$tooLongDescription"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin can clear description`() {
        val response = ExerciseResponse(
            id = 1L,
            name = "Squat",
            description = null,
            category = "Legs"
        )

        `when`(
            exerciseService.updateDescription(1L, null)
        ).thenReturn(response)

        mockMvc.perform(patch("/api/v1/fittrack/exercises/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                      "description": null
                    }
                    """.trimIndent()
            )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description").doesNotExist())

        verify(exerciseService).updateDescription(1L, null)
    }
}