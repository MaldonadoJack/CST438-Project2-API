package com.fittrack.api.admin

import com.fittrack.api.user.UserService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fittrack/admin")
class AdminController(
    private val userService: UserService
) {
    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/users")
    fun getUsers(
        @PageableDefault(
            size = 20,
            sort = ["id"],
            direction = Sort.Direction.ASC
        )
        pageable: Pageable
    ) = userService.getAllUsers(pageable)
}
