package com.authflix.authflix.service;

import com.authflix.authflix.model.User;
import com.authflix.authflix.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * This service is responsible for loading user-specific data by username (email in this case).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructs a UserDetailsServiceImpl with the given UserRepository.
     * @param userRepository The repository for accessing user data.
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the username (email). In the actual authentication process,
     * this method is called by Spring Security to load user details.
     *
     * @param username The email of the user attempting to authenticate.
     * @return A Spring Security UserDetails object.
     * @throws UsernameNotFoundException if the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by email in the database
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Return Spring Security's UserDetails object.
        // The password stored here must be the hashed password from the database.
        // The roles/authorities are empty for now but can be extended.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>() // Empty list of authorities/roles for now
        );
    }
}
