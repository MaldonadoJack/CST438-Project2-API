package com.fittrack.api.user

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/fittrack/users")
class UserController(
    private val userService: UserService
) {
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
