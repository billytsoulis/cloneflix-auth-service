package com.authflix.authflix.repository;

import com.authflix.authflix.model.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for the WatchlistItem entity.
 * Provides standard CRUD operations and custom query methods for watchlist items.
 */
@Repository // Marks this interface as a Spring Data repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    /**
     * Finds all watchlist items for a specific user.
     * @param userId The ID of the user.
     * @return A list of WatchlistItem objects belonging to the user.
     */
    List<WatchlistItem> findByUserId(Long userId);

    /**
     * Finds a specific watchlist item by user ID and movie ID.
     * This is useful to check if a movie is already in a user's watchlist.
     * @param userId The ID of the user.
     * @param movieId The ID of the movie (from FastAPI service).
     * @return An Optional containing the WatchlistItem if found, or empty if not.
     */
    Optional<WatchlistItem> findByUserIdAndMovieId(Long userId, int movieId);

    /**
     * Deletes a watchlist item by user ID and movie ID.
     * @param userId The ID of the user.
     * @param movieId The ID of the movie to remove from the watchlist.
     */
    void deleteByUserIdAndMovieId(Long userId, int movieId);
}
