package com.fittrack.api

import com.fittrack.api.user.User
import com.fittrack.api.user.UserRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = ["ADMIN"])
class AdminControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository
) {

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()

        userRepository.saveAll(
            listOf(
                User(
                    email = "admin@example.com",
                    displayName = "Admin User",
                    role = "admin"
                ),
                User(
                    email = "user1@example.com",
                    displayName = "Test User 1",
                    role = "user"
                ),
                User(
                    email = "user2@example.com",
                    displayName = "Test User 2",
                    role = "user"
                )
            )
        )
    }

    @Test
    fun `returns a paginated list of users`() {
        mockMvc.perform(
            get("/fittrack/admin/users")
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
    fun `deletes a user successfully`() {
        val userToDelete = userRepository.findByEmail("user1@example.com")!!

        mockMvc.perform(
            delete("/fittrack/admin/users/${userToDelete.id}")
                .with(csrf()) // adding csrf since it's a delete request and security is enabled
        )
            .andExpect(status().isNoContent)

        assertFalse(userRepository.findById(userToDelete.id!!).isPresent)
    }

    @Test
    fun `admin deletes user through versioned endpoint`() {
        val userToDelete = userRepository.findByEmail("user1@example.com")!!

        mockMvc.perform(
            delete("/api/v1/fittrack/users")
                .param("id", userToDelete.id.toString())
        )
            .andExpect(status().isNoContent)

        assertFalse(userRepository.findById(userToDelete.id!!).isPresent)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `regular user cannot delete another user`() {
        val userToDelete = userRepository.findByEmail("user1@example.com")!!

        mockMvc.perform(
            delete("/api/v1/fittrack/users")
                .param("id", userToDelete.id.toString())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithAnonymousUser
    fun `anonymous user cannot delete a user`() {
        mockMvc.perform(
            delete("/api/v1/fittrack/users")
                .param("id", "1")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `deleting missing user returns not found`() {
        mockMvc.perform(
            delete("/api/v1/fittrack/users")
                .param("id", "999")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `invalid user id is rejected`() {
        mockMvc.perform(
            delete("/api/v1/fittrack/users")
                .param("id", "0")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `admin can promote a user to admin`() {
        val user = userRepository.findByEmail("user1@example.com")!!

        mockMvc.perform(
            patch("/fittrack/admin/users/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"role":"admin"""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(user.id))
            .andExpect(jsonPath("$.role").value("admin"))

        val updatedUser = userRepository.findById(user.id!!).get()
        assertEquals("admin", updatedUser.role)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `regular user cannot promote another user`() {
        val user = userRepository.findByEmail("user1@example.com")!!

        mockMvc.perform(
            patch("/fittrack/admin/users/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"role":"admin"}""")
        )
            .andExpect(status().isForbidden)

        assertEquals("user", userRepository.findById(user.id!!).get().role)
    }

    @Test
    fun `promoting a missing user returns not found`() {
        mockMvc.perform(
            patch("/fittrack/admin/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"role":"admin"}""")
        )
            .andExpect(status().isNotFound)
    }
}
