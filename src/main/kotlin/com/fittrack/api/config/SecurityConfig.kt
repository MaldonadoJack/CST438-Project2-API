package com.fittrack.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class SecurityConfig {
    @Bean
    fun jwtDecoder(
        @Value("\${GOOGLE_CLIENT_ID:YOUR_WEB_CLIENT_ID.apps.googleusercontent.com}")
        googleClientId: String
    ): JwtDecoder {
        val decoder = NimbusJwtDecoder
            .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .build()

        val issuerValidator = JwtValidators.createDefaultWithIssuer("https://accounts.google.com")
        val audienceValidator = OAuth2TokenValidator<Jwt> { jwt ->
            if (jwt.audience.contains(googleClientId)) {
                OAuth2TokenValidatorResult.success()
            } else {
                OAuth2TokenValidatorResult.failure(
                    OAuth2Error("invalid_token", "Google token audience does not match", null)
                )
            }
        }

        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(issuerValidator, audienceValidator)
        )
        return decoder
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, mapper: ObjectMapper): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { rules ->
                rules.requestMatchers(HttpMethod.POST, "/api/v1/fittrack/exercises").hasRole("ADMIN")
                rules.requestMatchers(HttpMethod.PUT, "/api/v1/fittrack/exercises").hasRole("ADMIN")
                rules.requestMatchers(HttpMethod.DELETE, "/api/v1/fittrack/exercises").hasRole("ADMIN")
                rules.requestMatchers(HttpMethod.GET, "/api/v1/fittrack/exercises").permitAll()
                rules.requestMatchers(HttpMethod.GET, "/api/v1/fittrack/exercises").permitAll()
                rules.requestMatchers(HttpMethod.PATCH, "/api/v1/fittrack/exercises/*").hasRole("ADMIN")
                rules.requestMatchers("/fittrack/admin/**").hasRole("ADMIN")
                rules.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt(withDefaults())
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
