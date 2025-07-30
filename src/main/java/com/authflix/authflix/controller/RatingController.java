package com.authflix.authflix.controller;

import com.authflix.authflix.model.Rating;
import com.authflix.authflix.model.User;
import com.authflix.authflix.service.RatingService;
import com.authflix.authflix.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for managing user ratings for movies.
 * Provides endpoints for adding/updating, retrieving, and getting average ratings.
 */
@RestController
@RequestMapping("/api/ratings") // Base path for all rating-related endpoints
public class RatingController {

    private static final Logger log = LoggerFactory.getLogger(RatingController.class);

    private final RatingService ratingService;
    private final UserService userService; // To get the full User object from UserDetails

    /**
     * Constructs a RatingController with the necessary services.
     * @param ratingService The service for rating operations.
     * @param userService The service for user-related operations.
     */
    public RatingController(RatingService ratingService, UserService userService) {
        this.ratingService = ratingService;
        this.userService = userService;
    }

    /**
     * DTO (Data Transfer Object) for submitting or updating a movie rating.
     * This class is used to receive the request payload from the frontend.
     */
    static class RatingRequest {
        private int movieId;
        private int ratingValue;

        // Getters and Setters
        public int getMovieId() {
            return movieId;
        }

        public void setMovieId(int movieId) {
            this.movieId = movieId;
        }

        public int getRatingValue() {
            return ratingValue;
        }

        public void setRatingValue(int ratingValue) {
            this.ratingValue = ratingValue;
        }
    }

    /**
     * Adds a new rating or updates an existing rating for a movie by the authenticated user.
     * @param userDetails The authenticated user's details.
     * @param request The request body containing movie ID and rating value.
     * @return ResponseEntity with the saved/updated Rating object or an error message.
     */
    @PostMapping
    public ResponseEntity<?> addOrUpdateRating(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody RatingRequest request) {
        log.info("Attempting to add/update rating for user: {} on movie ID: {} with value: {}",
                userDetails.getUsername(), request.getMovieId(), request.getRatingValue());

        Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Authenticated user {} not found in database.", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        User user = userOptional.get();

        try {
            Rating savedRating = ratingService.addOrUpdateRating(user, request.getMovieId(), request.getRatingValue());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRating);
        } catch (IllegalArgumentException e) {
            log.warn("Rating submission failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while adding/updating rating: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process rating.");
        }
    }

    /**
     * Retrieves a specific rating given by the authenticated user for a movie.
     * @param userDetails The authenticated user's details.
     * @param movieId The ID of the movie to retrieve the rating for.
     * @return ResponseEntity with the Rating object or a 404 if not found.
     */
    @GetMapping("/{movieId}")
    public ResponseEntity<?> getUserRatingForMovie(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int movieId) {
        log.info("Fetching rating for user: {} on movie ID: {}", userDetails.getUsername(), movieId);

        Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Authenticated user {} not found in database.", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        User user = userOptional.get();

        Optional<Rating> rating = ratingService.getRatingByUserIdAndMovieId(user.getId(), movieId);
        if (rating.isPresent()) {
            return ResponseEntity.ok(rating.get());
        } else {
            log.info("No rating found for user {} on movie ID {}", userDetails.getUsername(), movieId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rating not found for this movie by this user.");
        }
    }

    /**
     * Retrieves the average rating for a specific movie.
     * This endpoint does not require user authentication.
     * @param movieId The ID of the movie to get the average rating for.
     * @return ResponseEntity with the average rating as a Double, or 204 No Content if no ratings exist.
     */
    @GetMapping("/average/{movieId}")
    public ResponseEntity<Double> getAverageRatingForMovie(@PathVariable int movieId) {
        log.info("Fetching average rating for movie ID: {}", movieId);
        Optional<Double> averageRating = ratingService.getAverageRatingForMovie(movieId);
        return averageRating.map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    /**
     * Deletes a specific rating given by the authenticated user for a movie.
     * @param userDetails The authenticated user's details.
     * @param movieId The ID of the movie to delete the rating for.
     * @return ResponseEntity indicating success or if the rating was not found.
     */
    @DeleteMapping("/{movieId}")
    public ResponseEntity<?> deleteRating(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int movieId) {
        log.info("Attempting to delete rating for user: {} on movie ID: {}", userDetails.getUsername(), movieId);

        Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Authenticated user {} not found in database.", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        User user = userOptional.get();

        boolean deleted = ratingService.deleteRating(user.getId(), movieId);
        if (deleted) {
            return ResponseEntity.ok("Rating deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rating not found for this movie by this user.");
        }
    }
}
