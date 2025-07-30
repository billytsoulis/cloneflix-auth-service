package com.authflix.authflix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a user's rating for a movie.
 * This entity links a specific user to a movie and stores the rating value.
 * It also includes timestamps for creation and last update.
 */
@Entity // Marks this class as a JPA entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "movie_id"}) // Ensures a user can only rate a movie once
})
@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate an all-argument constructor
@Builder // Lombok annotation to generate a builder pattern for object creation
public class Rating {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures ID generation strategy (auto-increment)
    private Long id;

    // Many-to-one relationship with the User entity.
    // A user can give many ratings, but each rating belongs to one user.
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetching to avoid loading user data unnecessarily
    @JoinColumn(name = "user_id", nullable = false) // Defines the foreign key column in ratings table
    private User user;

    // Store the movie ID from the FastAPI service.
    // This is not a foreign key to a local movie table, but an identifier for the external movie data.
    @Column(nullable = false)
    private int movieId;

    // The rating value (e.g., from 1 to 5, or 1 to 10)
    @Column(nullable = false)
    private int ratingValue;

    // Automatically set the timestamp when the rating is created
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatically update the timestamp when the rating is modified
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
