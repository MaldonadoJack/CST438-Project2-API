package com.fittrack.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.http.MediaType
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import javax.sql.DataSource

@SpringBootTest
@AutoConfigureMockMvc
class ExerciseCreationTests {
    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var jdbc: JdbcTemplate
    @Autowired lateinit var dataSource: DataSource

    @BeforeEach
    fun schema() {
        jdbc.execute("DROP ALL OBJECTS")
        // H2 requires an alias for PostgreSQL's TIMESTAMPTZ spelling.
        jdbc.execute("CREATE DOMAIN TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE")
        ResourceDatabasePopulator(FileSystemResource("database/schema.sql")).execute(dataSource)
    }

    private fun request(body: String) = post("/api/v1/fittrack/exercises")
        .contentType(MediaType.APPLICATION_JSON).content(body)

    private fun replaceRequest(body: String) = put("/api/v1/fittrack/exercises")
        .contentType(MediaType.APPLICATION_JSON).content(body)

    private fun deleteRequest(id: Long) = delete("/api/v1/fittrack/exercises")
        .param("id", id.toString())

    @Test
    fun adminCreatesAndPersistsExercise() {
        mvc.perform(request("""{"name":" Squat ","description":"Lower body","category":"strength"}""")
            .with(user("admin").roles("ADMIN")))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name").value("Squat"))
            .andExpect(jsonPath("$.description").value("Lower body"))
            .andExpect(jsonPath("$.category").value("strength"))
        assertEquals("Squat", jdbc.queryForObject("SELECT name FROM exercises", String::class.java))
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(created_at) FROM exercises", Int::class.java))
    }

    @Test
    fun optionalFieldsMayBeOmitted() {
        mvc.perform(request("""{"name":"Squat"}""").with(user("admin").roles("ADMIN")))
            .andExpect(status().isCreated)
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM exercises", Int::class.java))
    }

    @Test
    fun anonymousAndNonAdminCannotInsert() {
        mvc.perform(request("""{"name":"Squat"}""")).andExpect(status().isUnauthorized)
        mvc.perform(request("""{"name":"Squat"}""").with(user("user").roles("USER")))
            .andExpect(status().isForbidden)
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM exercises", Int::class.java))
    }

    @Test
    fun invalidInputDoesNotInsert() {
        val invalid = listOf("""{"name":"   "}""", "{}", """{"name":null}""", "{",
            """{"name":"${"x".repeat(101)}"}""",
            """{"name":"Squat","description":"${"x".repeat(1001)}"}""",
            """{"name":"Squat","category":"${"x".repeat(101)}"}""")
        invalid.forEach {
            mvc.perform(request(it).with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM exercises", Int::class.java))
    }

    @Test
    fun duplicateNameReturnsConflict() {
        repeat(2) { index ->
            mvc.perform(request("""{"name":"Squat"}""").with(user("admin").roles("ADMIN")))
                .andExpect(status().`is`(if (index == 0) 201 else 409))
        }
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM exercises", Int::class.java))
    }

    @Test
    fun adminReplacesAndPersistsExercise() {
        jdbc.update(
            "INSERT INTO exercises (name, description, category) VALUES (?, ?, ?)",
            "Squat",
            "Old description",
            "strength"
        )
        val id = jdbc.queryForObject("SELECT id FROM exercises", Long::class.java)!!

        mvc.perform(
            replaceRequest(
                """{"id":$id,"name":" Front Squat ","description":"Updated description","category":"legs"}"""
            ).with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("Front Squat"))
            .andExpect(jsonPath("$.description").value("Updated description"))
            .andExpect(jsonPath("$.category").value("legs"))

        assertEquals(
            "Front Squat",
            jdbc.queryForObject("SELECT name FROM exercises WHERE id = ?", String::class.java, id)
        )
        assertEquals(
            "Updated description",
            jdbc.queryForObject("SELECT description FROM exercises WHERE id = ?", String::class.java, id)
        )
        assertEquals(
            "legs",
            jdbc.queryForObject("SELECT category FROM exercises WHERE id = ?", String::class.java, id)
        )
    }

    @Test
    fun anonymousAndNonAdminCannotReplaceExercise() {
        val body = """{"id":1,"name":"Front Squat"}"""

        mvc.perform(replaceRequest(body)).andExpect(status().isUnauthorized)
        mvc.perform(replaceRequest(body).with(user("user").roles("USER")))
            .andExpect(status().isForbidden)
    }

    @Test
    fun invalidReplacementIsRejected() {
        val invalid = listOf(
            """{"id":0,"name":"Squat"}""",
            """{"id":1,"name":"   "}""",
            """{"id":1,"name":"${"x".repeat(101)}"}""",
            """{"id":1,"name":"Squat","description":"${"x".repeat(1001)}"}""",
            """{"id":1,"name":"Squat","category":"${"x".repeat(101)}"}"""
        )

        invalid.forEach {
            mvc.perform(replaceRequest(it).with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest)
        }
    }

    @Test
    fun replacingMissingExerciseReturnsNotFound() {
        mvc.perform(
            replaceRequest("""{"id":999,"name":"Squat"}""")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
    }

    @Test
    fun replacementWithDuplicateNameReturnsConflict() {
        jdbc.update("INSERT INTO exercises (name) VALUES (?)", "Squat")
        jdbc.update("INSERT INTO exercises (name) VALUES (?)", "Deadlift")
        val deadliftId = jdbc.queryForObject(
            "SELECT id FROM exercises WHERE name = 'Deadlift'",
            Long::class.java
        )!!

        mvc.perform(
            replaceRequest("""{"id":$deadliftId,"name":"Squat"}""")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isConflict)

        assertEquals(
            "Deadlift",
            jdbc.queryForObject("SELECT name FROM exercises WHERE id = ?", String::class.java, deadliftId)
        )
    }

    @Test
    fun adminDeletesExercise() {
        jdbc.update("INSERT INTO exercises (name) VALUES (?)", "Squat")
        val id = jdbc.queryForObject("SELECT id FROM exercises", Long::class.java)!!

        mvc.perform(deleteRequest(id).with(user("admin").roles("ADMIN")))
            .andExpect(status().isNoContent)

        assertEquals(
            0,
            jdbc.queryForObject("SELECT COUNT(*) FROM exercises WHERE id = ?", Int::class.java, id)
        )
    }

    @Test
    fun anonymousAndNonAdminCannotDeleteExercise() {
        mvc.perform(deleteRequest(1)).andExpect(status().isUnauthorized)
        mvc.perform(deleteRequest(1).with(user("user").roles("USER")))
            .andExpect(status().isForbidden)
    }

    @Test
    fun deletingMissingExerciseReturnsNotFound() {
        mvc.perform(deleteRequest(999).with(user("admin").roles("ADMIN")))
            .andExpect(status().isNotFound)
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
    }

    @Test
    fun invalidDeleteIdIsRejected() {
        mvc.perform(deleteRequest(0).with(user("admin").roles("ADMIN")))
            .andExpect(status().isBadRequest)
    }
}
