package com.authflix.authflix.config;

import com.authflix.authflix.filter.JwtRequestFilter;
import com.authflix.authflix.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Keep this import
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll() // Allow public access to register and login
                .anyRequest().authenticated() // All other requests require authentication
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions for JWT
            )
            // Configure logout specifically
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout") // Define the logout URL
                .invalidateHttpSession(true) // Invalidate session (though stateless, good practice)
                .deleteCookies("jwt") // Explicitly delete the JWT cookie
                // Define a success handler to ensure a clear response is sent
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Logged out successfully.");
                    response.getWriter().flush();
                })
                .permitAll() // Allow all to access logout endpoint, even unauthenticated
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
     * This is the modern way to configure AuthenticationManager with UserDetailsService and PasswordEncoder.
     * Spring Boot will automatically use a DaoAuthenticationProvider internally.
     * @param authenticationConfiguration The AuthenticationConfiguration.
     * @return An AuthenticationManager instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // This line is the key change. Spring Boot's AuthenticationConfiguration
        // will automatically use the UserDetailsService and PasswordEncoder beans
        // to configure the DaoAuthenticationProvider internally.
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Removed the explicit authenticationProvider() bean as it's no longer needed
    // with the updated authenticationManager configuration.
}
