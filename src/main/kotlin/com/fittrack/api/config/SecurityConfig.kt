package com.fittrack.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    // No generated development password or login bypass. The team's future
    // authentication integration must supply a principal with ROLE_ADMIN.
    @Bean
    fun userDetailsService(): UserDetailsService = UserDetailsService {
        throw UsernameNotFoundException("Authentication provider has not been configured")
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, mapper: ObjectMapper): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { rules ->
                rules.requestMatchers(HttpMethod.POST, "/api/v1/fittrack/exercises").hasRole("ADMIN")
                rules.requestMatchers(HttpMethod.GET, "/api/v1/fittrack/exercises").permitAll()
                rules.requestMatchers(HttpMethod.GET, "/fittrack/exercises").permitAll()
                rules.anyRequest().authenticated()
            }
            .exceptionHandling { errors ->
                errors.authenticationEntryPoint { _, response, _ ->
                    response.status = 401
                    response.contentType = "application/problem+json"
                    mapper.writeValue(response.outputStream, mapOf("title" to "Unauthorized", "status" to 401))
                }
                errors.accessDeniedHandler { _, response, _ ->
                    response.status = 403
                    response.contentType = "application/problem+json"
                    mapper.writeValue(response.outputStream, mapOf("title" to "Forbidden", "status" to 403))
                }
            }
        return http.build()
    }
}
