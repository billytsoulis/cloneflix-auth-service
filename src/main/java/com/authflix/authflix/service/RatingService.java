package com.authflix.authflix.service;

import com.authflix.authflix.model.Rating;
import com.authflix.authflix.model.User;
import com.authflix.authflix.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Rating operations.
 * This class provides methods to add, update, and retrieve user ratings for movies.
 */
@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final UserService userService; // To fetch User entity by ID if needed

    /**
     * Constructs a RatingService with the given RatingRepository and UserService.
     * @param ratingRepository The repository for accessing rating data.
     * @param userService The service for accessing user data.
     */
    public RatingService(RatingRepository ratingRepository, UserService userService) {
        this.ratingRepository = ratingRepository;
        this.userService = userService;
    }

    /**
     * Adds a new rating or updates an existing one for a movie by a specific user.
     * If a rating for the given user and movie already exists, it updates the rating value.
     * Otherwise, it creates a new rating.
     *
     * @param user The user who is submitting the rating.
     * @param movieId The ID of the movie being rated.
     * @param ratingValue The rating value (e.g., 1-5 or 1-10).
     * @return The saved or updated Rating object.
     */
    @Transactional
    public Rating addOrUpdateRating(User user, int movieId, int ratingValue) {
        // Validate rating value (e.g., between 1 and 10)
        if (ratingValue < 1 || ratingValue > 10) {
            log.warn("Invalid rating value {} for user {} and movie {}. Rating must be between 1 and 10.", ratingValue, user.getEmail(), movieId);
            throw new IllegalArgumentException("Rating value must be between 1 and 10.");
        }

        Optional<Rating> existingRating = ratingRepository.findByUserIdAndMovieId(user.getId(), movieId);

        Rating rating;
        if (existingRating.isPresent()) {
            rating = existingRating.get();
            rating.setRatingValue(ratingValue); // Update existing rating
            log.info("Updated rating for user {} on movie {}: new rating {}", user.getEmail(), movieId, ratingValue);
        } else {
            rating = Rating.builder()
                    .user(user)
                    .movieId(movieId)
                    .ratingValue(ratingValue)
                    .build();
            log.info("Added new rating for user {} on movie {}: rating {}", user.getEmail(), movieId, ratingValue);
        }
        return ratingRepository.save(rating);
    }

    /**
     * Retrieves a specific rating given by a user for a movie.
     * @param userId The ID of the user.
     * @param movieId The ID of the movie.
     * @return An Optional containing the Rating if found, or empty.
     */
    public Optional<Rating> getRatingByUserIdAndMovieId(Long userId, int movieId) {
        log.debug("Fetching rating for user {} on movie {}", userId, movieId);
        return ratingRepository.findByUserIdAndMovieId(userId, movieId);
    }

    /**
     * Retrieves all ratings given by a specific user.
     * @param userId The ID of the user.
     * @return A list of Rating objects given by the user.
     */
    public List<Rating> getRatingsByUserId(Long userId) {
        log.debug("Fetching all ratings for user {}", userId);
        return ratingRepository.findByUserId(userId);
    }

    /**
     * Deletes a specific rating given by a user for a movie.
     * @param userId The ID of the user.
     * @param movieId The ID of the movie.
     * @return true if the rating was found and deleted, false otherwise.
     */
    @Transactional
    public boolean deleteRating(Long userId, int movieId) {
        Optional<Rating> ratingToDelete = ratingRepository.findByUserIdAndMovieId(userId, movieId);
        if (ratingToDelete.isPresent()) {
            ratingRepository.delete(ratingToDelete.get());
            log.info("Deleted rating for user {} on movie {}", userId, movieId);
            return true;
        }
        log.warn("Rating not found for user {} on movie {}. Nothing to delete.", userId, movieId);
        return false;
    }

    /**
     * Calculates the average rating for a specific movie.
     * @param movieId The ID of the movie.
     * @return The average rating as a Double, or Optional.empty() if no ratings exist.
     */
    public Optional<Double> getAverageRatingForMovie(int movieId) {
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        if (ratings.isEmpty()) {
            return Optional.empty();
        }
        double sum = ratings.stream().mapToInt(Rating::getRatingValue).sum();
        return Optional.of(sum / ratings.size());
    }
}
