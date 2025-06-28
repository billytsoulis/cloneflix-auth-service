package com.authflix.authflix.service;

import com.authflix.authflix.model.User;
import com.authflix.authflix.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.encoder = new BCryptPasswordEncoder();
    }

    public User register(String email, String rawPassword) {
        String encodedPassword = encoder.encode(rawPassword);
        User user = User.builder().email(email).password(encodedPassword).build();
        return userRepository.save(user);
    }

    public boolean validateCredentials(String email, String rawPassword) {
        return userRepository.findByEmail(email)
            .map(user -> encoder.matches(rawPassword, user.getPassword()))
            .orElse(false);
    }
}
