package com.fittrack.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GreetingController {

    @GetMapping("/")
    fun home(): String {
        return "Welcome to the Fitness Tracker API!"
    }

    @GetMapping("/greeting")
    fun greeting(): String {
        return "Hello! Your Fitness Tracker API is running!"
    }
}

