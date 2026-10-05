package com.fittrack.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FitTrackApiApplication

fun main(args: Array<String>) {
    runApplication<FitTrackApiApplication>(*args)
}
