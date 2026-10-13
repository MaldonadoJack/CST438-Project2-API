package com.fittrack.api.auth

import com.fittrack.api.user.UserProfileResponse
import com.fittrack.api.user.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/fittrack/auth")
class GoogleAuthController(
    private val userService: UserService
) {
    @PostMapping("/google")
    fun signInWithGoogle(
        @AuthenticationPrincipal jwt: Jwt
    ): UserProfileResponse {
        val emailVerified = jwt.claims["email_verified"] as? Boolean ?: false
        if (!emailVerified) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified")
        }

        val email = jwt.getClaimAsString("email")
            ?.takeIf { it.isNotBlank() }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is required")

        val displayName = jwt.getClaimAsString("name")
            ?.takeIf { it.isNotBlank() }
            ?: email.substringBefore('@')

        return userService.getOrCreateGoogleUser(email, displayName)
    }
}
