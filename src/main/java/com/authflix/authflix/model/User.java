package com.authflix.authflix.model;

import jakarta.persistence.*;
import lombok.*; // Ensure @Builder is included by lombok.* or specifically lombok.Builder

/**
 * Represents a User entity in the Authflix application.
 * This class maps to a database table and holds user-specific information
 * such as ID, email, and password.
 */
@Entity // Marks this class as a JPA entity
@Table(name = "users") // Specifies the table name in the database
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate an all-argument constructor
@Builder // <--- IMPORTANT: This annotation is needed for User.builder() to work
public class User {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures the primary key generation strategy
    private Long id;

    @Column(unique = true, nullable = false) // Defines column constraints: unique and not null
    private String email;

    @Column(nullable = false) // Defines column constraint: not null
    private String password; // Storing hashed password

    // You can add more user-related fields here if needed.
}
