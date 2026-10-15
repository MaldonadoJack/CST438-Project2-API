package com.fittrack.api.progress

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/fittrack/progress")
class ProgressController(
    private val progressService: ProgressService
) {

    @PostMapping
    fun recordProgress(
        @RequestBody request: ProgressRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ProgressResponse> {

        val email = jwt.getClaimAsString("email")
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val response = progressService.recordProgress(request, email)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getMyProgress(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<ProgressResponse>> {

        val email = jwt.getClaimAsString("email")
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(
            progressService.getMyProgress(email)
        )
    }
}
