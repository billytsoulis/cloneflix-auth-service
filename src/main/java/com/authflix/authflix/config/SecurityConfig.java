package com.authflix.authflix.config;

import com.authflix.authflix.filter.JwtRequestFilter; // Import the JwtRequestFilter
import com.authflix.authflix.service.UserDetailsServiceImpl; // Import UserDetailsServiceImpl
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import UsernamePasswordAuthenticationFilter

/**
 * Security configuration for the Authflix application.
 * This class sets up the security filters, authentication providers,
 * and authorization rules for HTTP requests, including JWT integration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Constructor for SecurityConfig, injecting UserDetailsServiceImpl and JwtRequestFilter.
     * @param userDetailsService The custom UserDetailsService.
     * @param jwtRequestFilter The custom JWT authentication filter.
     */
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtRequestFilter jwtRequestFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * Configures the security filter chain.
     * Defines authorization rules for various endpoints, sets up session management,
     * and integrates the custom JWT filter.
     *
     * @param http The HttpSecurity object to configure.
     * @return A SecurityFilterChain instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for API endpoints (common with JWT)
            .cors(Customizer.withDefaults()) // Use custom CORS configuration from CorsConfig
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**").permitAll() // Allow public access to authentication endpoints (register, login)
                .anyRequest().authenticated() // All other requests require authentication
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions for JWT
            );

        // Add the custom JWT filter before the Spring Security's UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides a BCryptPasswordEncoder bean for password hashing.
     * @return An instance of BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager as a Bean.
     * This is needed by the UserController to perform authentication.
     * Spring Boot's auto-configuration will use the UserDetailsService and PasswordEncoder
     * beans provided in this configuration to build the AuthenticationManager.
     * @param authenticationConfiguration The AuthenticationConfiguration.
     * @return An AuthenticationManager instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
