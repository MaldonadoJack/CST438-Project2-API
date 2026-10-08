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
}
