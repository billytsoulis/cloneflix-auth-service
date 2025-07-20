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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);

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
      log.debug("Authenticating user with AuthenticationManager...");
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
      );
      log.debug("User authenticated successfully by AuthenticationManager.");

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String token = jwtUtil.generateToken(userDetails.getUsername());
      log.debug("JWT token generated successfully for user: {}", userDetails.getUsername());

      Cookie jwtCookie = new Cookie("jwt", token);
      jwtCookie.setHttpOnly(true);
      jwtCookie.setSecure(false);
      jwtCookie.setMaxAge((int) (jwtUtil.getExpiration() / 1000));
      jwtCookie.setPath("/");

      response.addCookie(jwtCookie);
      log.info("HttpOnly JWT cookie set successfully for user: {}", userDetails.getUsername());

      return ResponseEntity.ok("Login successful, token set in HttpOnly cookie.");

    } catch (Exception e) {
      log.error("Login failed for user: {} with error: {}", user.getEmail(), e.getMessage(), e);
      return ResponseEntity.status(401).body("Invalid credentials");
    }
  }

  /**
   * Logs out the user by clearing the JWT cookie.
   * Sets the cookie's max age to 0 to ensure immediate deletion by the browser.
   * This method should be ignored by Spring Security filters (configured in SecurityConfig)
   * to ensure it can always execute and clear the cookie regardless of token validity.
   * @param response The HttpServletResponse to add the cookie to.
   * @return ResponseEntity indicating successful logout.
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
      log.info("Attempting to log out user and clear cookie.");
      Cookie jwtCookie = new Cookie("jwt", null); // Set value to null
      jwtCookie.setHttpOnly(true);
      jwtCookie.setSecure(false); // Same as login: false for local HTTP, true for production HTTPS
      jwtCookie.setPath("/");
      jwtCookie.setMaxAge(0); // Set max age to 0 for immediate deletion

      response.addCookie(jwtCookie); // Use addCookie to let Spring handle header formatting

      log.info("Logout successful, JWT cookie cleared.");
      return ResponseEntity.ok("Logged out successfully, cookie cleared.");
  }

  /**
   * Endpoint to get the profile of the currently authenticated user.
   * @param userDetails The authenticated user's details injected by Spring Security.
   * @return ResponseEntity containing the user's email and ID.
   */
  @GetMapping("/profile")
  public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
      log.info("Fetching profile for user: {}", userDetails.getUsername());
      Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());
      if (userOptional.isPresent()) {
          return ResponseEntity.ok(userOptional.get());
      } else {
          log.error("Profile not found for authenticated user: {}", userDetails.getUsername());
          return ResponseEntity.status(404).body("User profile not found.");
      }
  }

  /**
   * Endpoint to update the profile of the currently authenticated user.
   * Allows updating email and/or password.
   * @param userDetails The authenticated user's details.
   * @param updatedUserDto A DTO containing updated email, current password, and new password.
   * @return ResponseEntity indicating success or failure.
   */
  @PutMapping("/profile")
  public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UpdateUserDto updatedUserDto) {
      log.info("Attempting to update profile for user: {}", userDetails.getUsername());
      try {
          User currentUser = userService.findByEmail(userDetails.getUsername())
                  .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB."));

          boolean emailChanged = false;
          boolean passwordChanged = false;

          // Check and update email if provided and different
          if (updatedUserDto.getEmail() != null && !updatedUserDto.getEmail().isEmpty() && !updatedUserDto.getEmail().equals(currentUser.getEmail())) {
              if (userService.findByEmail(updatedUserDto.getEmail()).isPresent()) {
                  log.warn("Profile update failed: New email {} already exists.", updatedUserDto.getEmail());
                  return ResponseEntity.badRequest().body("New email already in use.");
              }
              currentUser.setEmail(updatedUserDto.getEmail());
              emailChanged = true;
              log.info("User {} changed email to {}.", userDetails.getUsername(), updatedUserDto.getEmail());
          }

          // Check and update password if provided
          if (updatedUserDto.getNewPassword() != null && !updatedUserDto.getNewPassword().isEmpty()) {
              // Mandate current password for password change for security
              if (updatedUserDto.getCurrentPassword() == null || updatedUserDto.getCurrentPassword().isEmpty()) {
                  log.warn("Password update failed for user {}: Current password not provided.", userDetails.getUsername());
                  return ResponseEntity.badRequest().body("Current password is required to change password.");
              }
              // Verify current password using the new checkPassword method
              if (!userService.checkPassword(currentUser, updatedUserDto.getCurrentPassword())) {
                  log.warn("Password update failed for user {}: Incorrect current password.", userDetails.getUsername());
                  return ResponseEntity.status(401).body("Incorrect current password.");
              }
              userService.updatePassword(currentUser, updatedUserDto.getNewPassword());
              passwordChanged = true;
              log.info("User {} updated password.", userDetails.getUsername());
          }

          // Save the updated user if any changes were made
          if (emailChanged || passwordChanged) {
              User savedUser = userService.saveUser(currentUser);
              log.info("Profile for user {} updated successfully.", savedUser.getEmail());
              return ResponseEntity.ok("Profile updated successfully.");
          } else {
              log.info("No changes detected for user {}.", userDetails.getUsername());
              return ResponseEntity.ok("No changes to apply.");
          }

      } catch (Exception e) {
          log.error("Failed to update profile for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
          return ResponseEntity.status(500).body("Failed to update profile: " + e.getMessage());
      }
  }
}

// DTO (Data Transfer Object) for UpdateUser request
// This class is used to receive the update payload from the frontend.
// It includes currentPassword for security when changing password.
class UpdateUserDto {
    private String email;
    private String currentPassword; // Required when changing password
    private String newPassword;     // New password

    // Getters and Setters (or use Lombok @Data, @Getter, @Setter)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
