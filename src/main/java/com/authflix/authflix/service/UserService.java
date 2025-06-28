package com.authflix.authflix.service;

import com.authflix.authflix.model.User;
import com.authflix.authflix.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Use the interface

    /**
     * Injects UserRepository and PasswordEncoder.
     * Spring will provide the BCryptPasswordEncoder bean defined in SecurityConfig.
     * @param userRepository The repository for user data.
     * @param passwordEncoder The password encoder bean.
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // Assign the injected encoder
    }

    public User register(String email, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword); // Use the injected encoder
        User user = User.builder().email(email).password(encodedPassword).build();
        return userRepository.save(user);
    }

    public boolean validateCredentials(String email, String rawPassword) {
        return userRepository.findByEmail(email)
            .map(user -> passwordEncoder.matches(rawPassword, user.getPassword())) // Use the injected encoder
            .orElse(false);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
