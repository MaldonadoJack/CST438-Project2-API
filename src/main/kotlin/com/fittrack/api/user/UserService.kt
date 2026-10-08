package com.fittrack.api.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
}
