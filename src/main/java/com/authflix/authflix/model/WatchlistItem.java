package com.authflix.authflix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity // Marks this class as a JPA entity
@Table(name = "watchlist_items") // Defines the table name in the database
@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate an all-argument constructor
@Builder // Lombok annotation to generate a builder pattern for object creation
public class WatchlistItem {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures ID generation strategy (auto-increment)
    private Long id;

    // Many-to-one relationship with the User entity.
    // A user can have many watchlist items, but each watchlist item belongs to one user.
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetching to avoid loading user data unnecessarily
    @JoinColumn(name = "user_id", nullable = false) // Defines the foreign key column in watchlist_items table
    private User user;

    // Store the movie ID from the FastAPI service.
    // This is not a foreign key to a local movie table, but an identifier for the external movie data.
    @Column(nullable = false)
    private int movieId;

    // Store basic movie details to avoid frequent calls to the FastAPI service for display
    @Column(nullable = false)
    private String movieTitle;

    @Column(nullable = false)
    private String movieGenre;

    // Automatically set the timestamp when the item is added to the watchlist
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
