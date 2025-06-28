package com.authflix.authflix.controller;

import com.authflix.authflix.model.User;
import com.authflix.authflix.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody User user) {
    return ResponseEntity.ok(userService.register(user.getEmail(), user.getPassword()));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody User user) {
    boolean valid = userService.validateCredentials(user.getEmail(), user.getPassword());
    return valid
        ? ResponseEntity.ok("Login successful")
        : ResponseEntity.status(401).body("Invalid credentials");
  }

}
