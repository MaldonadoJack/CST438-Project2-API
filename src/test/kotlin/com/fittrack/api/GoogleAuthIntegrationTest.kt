package com.fittrack.api

import com.fittrack.api.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleAuthIntegrationTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @BeforeEach
    fun clearUsers() {
        userRepository.deleteAll()
    }

    @Test
    fun verifiedGoogleUserCanSignInAndCreateProfile() {
        mvc.perform(
            post("/api/v1/fittrack/auth/google")
                .with(
                    jwt().jwt { token ->
                        token.claim("email", "student@example.com")
                        token.claim("name", "Student User")
                        token.claim("email_verified", true)
                    }
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("student@example.com"))
            .andExpect(jsonPath("$.displayName").value("Student User"))

        assertEquals(1, userRepository.count())
    }

    @Test
    fun repeatGoogleSignInUsesTheExistingProfile() {
        val request = post("/api/v1/fittrack/auth/google")
            .with(
                jwt().jwt { token ->
                    token.claim("email", "student@example.com")
                    token.claim("name", "Student User")
                    token.claim("email_verified", true)
                }
            )

        mvc.perform(request).andExpect(status().isOk)
        mvc.perform(request).andExpect(status().isOk)

        assertEquals(1, userRepository.count())
    }

    @Test
    fun unverifiedGoogleEmailIsRejected() {
        mvc.perform(
            post("/api/v1/fittrack/auth/google")
                .with(
                    jwt().jwt { token ->
                        token.claim("email", "student@example.com")
                        token.claim("email_verified", false)
                    }
                )
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun missingGoogleTokenIsRejected() {
        mvc.perform(post("/api/v1/fittrack/auth/google"))
            .andExpect(status().isUnauthorized)
    }
}
