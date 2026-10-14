package com.fittrack.api.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    fun getAllUsers(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    @Transactional
    fun getOrCreateGoogleUser(email: String, displayName: String): UserProfileResponse {
        val user = userRepository.findByEmail(email) ?: userRepository.save(
            User(
                email = email,
                displayName = displayName,
                role = "user"
            )
        )

        return UserProfileResponse(
            id = requireNotNull(user.id),
            email = user.email,
            displayName = user.displayName
        )
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
