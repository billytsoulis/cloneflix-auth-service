package com.authflix.authflix.repository;

import com.authflix.authflix.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for the Rating entity.
 * Provides standard CRUD operations and custom query methods for movie ratings.
 */
@Repository // Marks this interface as a Spring Data repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Finds a rating by a specific user for a specific movie.
     * This is useful to check if a user has already rated a movie.
     * @param userId The ID of the user.
     * @param movieId The ID of the movie (from FastAPI service).
     * @return An Optional containing the Rating if found, or empty if not.
     */
    Optional<Rating> findByUserIdAndMovieId(Long userId, int movieId);

    /**
     * Finds all ratings given by a specific user.
     * @param userId The ID of the user.
     * @return A list of Rating objects given by the user.
     */
    List<Rating> findByUserId(Long userId);

    /**
     * Finds all ratings for a specific movie.
     * This could be used to calculate an average rating for a movie.
     * @param movieId The ID of the movie.
     * @return A list of Rating objects for the specified movie.
     */
    List<Rating> findByMovieId(int movieId);
}
