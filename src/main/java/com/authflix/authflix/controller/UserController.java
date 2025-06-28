package com.authflix.authflix.controller;

import com.authflix.authflix.model.User;
import com.authflix.authflix.service.UserService;
import com.authflix.authflix.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

@RestController
@RequestMapping("/api/auth")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class); // Initialize Logger

  private final UserService userService;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;

  public UserController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
    this.userService = userService;
    this.jwtUtil = jwtUtil;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody User user) {
    log.info("Attempting to register user with email: {}", user.getEmail());
    if (userService.findByEmail(user.getEmail()).isPresent()) {
      log.warn("Registration failed: User with email {} already exists.", user.getEmail());
      return ResponseEntity.badRequest().body("User with this email already exists.");
    }
    userService.register(user.getEmail(), user.getPassword());
    log.info("User {} registered successfully.", user.getEmail());
    return ResponseEntity.ok("Registration successful. Please log in.");
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody User user, HttpServletResponse response) {
    log.info("Login attempt for user: {}", user.getEmail());
    try {
      // Step 1: Authenticate the user
      log.debug("Authenticating user with AuthenticationManager...");
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
      );
      log.debug("User authenticated successfully by AuthenticationManager.");

      // Step 2: Generate JWT token
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String token = jwtUtil.generateToken(userDetails.getUsername());
      log.debug("JWT token generated successfully for user: {}", userDetails.getUsername());

      // Step 3: Create and set HttpOnly cookie
      Cookie jwtCookie = new Cookie("jwt", token);
      jwtCookie.setHttpOnly(true);
      // IMPORTANT: For local development over HTTP, setSecure must be false.
      // For production with HTTPS, it MUST be true.
      jwtCookie.setSecure(false);
      jwtCookie.setMaxAge((int) (jwtUtil.getExpiration() / 1000)); // Set max age in seconds
      jwtCookie.setPath("/"); // Make the cookie available to all paths
      
      response.addCookie(jwtCookie);
      log.info("HttpOnly JWT cookie set successfully for user: {}", userDetails.getUsername());

      // Step 4: Return success response
      return ResponseEntity.ok("Login successful, token set in HttpOnly cookie.");

    } catch (Exception e) {
      log.error("Login failed for user: {} with error: {}", user.getEmail(), e.getMessage(), e);
      return ResponseEntity.status(401).body("Invalid credentials");
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
      log.info("Attempting to log out user and clear cookie.");
      Cookie jwtCookie = new Cookie("jwt", null);
      jwtCookie.setHttpOnly(true);
      jwtCookie.setSecure(false); // Same as login: true in prod with HTTPS
      jwtCookie.setMaxAge(0); // Immediately expire the cookie
      jwtCookie.setPath("/");
      response.addCookie(jwtCookie);
      log.info("Logout successful, JWT cookie cleared.");
      return ResponseEntity.ok("Logged out successfully, cookie cleared.");
  }

  // No longer needed since we return a simple string or the cookie directly
  // static class AuthResponse {
  //   private String token;
  //   public AuthResponse(String token) { this.token = token; }
  //   public String getToken() { return token; }
  //   public void setToken(String token) { this.token = token; }
  // }
}
