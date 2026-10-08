package com.fittrack.api

import com.fittrack.api.user.User
import com.fittrack.api.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserProfileIntegrationTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()

        userRepository.save(
            User(
                email = "student@example.com",
                displayName = "Student User",
                role = "user"
            )
        )
    }

    @Test
    fun authenticatedUserCanViewOwnProfile() {
        mvc.perform(
            get("/api/v1/fittrack/users/me")
                .with(user("student@example.com").roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("student@example.com"))
            .andExpect(jsonPath("$.displayName").value("Student User"))
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.role").doesNotExist())
    }

    @Test
    fun unauthenticatedUserCannotViewProfile() {
        mvc.perform(get("/api/v1/fittrack/users/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun missingUserProfileReturnsNotFound() {
        mvc.perform(
            get("/api/v1/fittrack/users/me")
                .with(user("missing@example.com").roles("USER"))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun userCannotViewAnotherUsersProfile() {
        userRepository.save(
            User(
                email = "another@example.com",
                displayName = "Another User",
                role = "user"
            )
        )

        mvc.perform(
            get("/api/v1/fittrack/users/me")
                .with(user("student@example.com").roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("student@example.com"))
            .andExpect(jsonPath("$.displayName").value("Student User"))
    }
}
