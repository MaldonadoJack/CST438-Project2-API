package com.fittrack.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {
    @Test
    fun `swagger UI is publicly available`() {
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().isFound)
            .andExpect(redirectedUrl("/swagger-ui/index.html"))

        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith("text/html"))
    }

    @Test
    fun `OpenAPI description is publicly available and includes exercises`() {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.paths['/api/v1/fittrack/exercises']").exists())
    }
}
