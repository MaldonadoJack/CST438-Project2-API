package com.fittrack.api.user

import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/fittrack/users")
class UserController(
    private val userService: UserService
) {
    @DeleteMapping
    fun deleteUser(
        @RequestParam @Positive id: Long
    ): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    fun getMyProfile(authentication: Authentication): UserProfileResponse {
        val email = authentication.name

        if (email.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated email is required"
            )
        }

        return userService.getMyProfile(email)
    }
}
