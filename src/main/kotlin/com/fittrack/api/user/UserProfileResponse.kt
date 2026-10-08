package com.fittrack.api.user

data class UserProfileResponse(
    val id: Long,
    val email: String,
    val displayName: String
)
