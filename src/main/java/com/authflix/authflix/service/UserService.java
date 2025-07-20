package com.authflix.authflix.service;

import com.authflix.authflix.model.User;
import com.authflix.authflix.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = User.builder().email(email).password(encodedPassword).build();
        return userRepository.save(user);
    }

    public boolean validateCredentials(String email, String rawPassword) {
        return userRepository.findByEmail(email)
            .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
            .orElse(false);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(User user, String newRawPassword) {
        String encodedPassword = passwordEncoder.encode(newRawPassword);
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    /**
     * New: Verifies if a given raw password matches the stored hashed password for a specific user.
     * This is used for current password validation during profile updates.
     * @param user The User object whose password needs to be verified.
     * @param rawPassword The plain-text password to verify.
     * @return true if the raw password matches the stored hashed password, false otherwise.
     */
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}