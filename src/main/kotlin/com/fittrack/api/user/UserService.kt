package com.fittrack.api.user

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun deleteUser(id: Long) {
        userRepository.deleteById(id)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getMyProfile(email: String): UserProfileResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User profile not found"
            )

        return UserProfileResponse(
            id = requireNotNull(user.id),
            email = user.email,
            displayName = user.displayName
        )
    }
}
