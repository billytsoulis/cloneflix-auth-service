package com.authflix.authflix.controller;

import com.authflix.authflix.model.User;
import com.authflix.authflix.model.WatchlistItem;
import com.authflix.authflix.service.UserService;
import com.authflix.authflix.service.WatchlistItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing user watchlists.
 * Provides endpoints for adding, removing, and retrieving watchlist items.
 */
@RestController
@RequestMapping("/api/watchlist") // Base path for all watchlist-related endpoints
public class WatchlistController {

    private static final Logger log = LoggerFactory.getLogger(WatchlistController.class);

    private final WatchlistItemService watchlistItemService;
    private final UserService userService; // To get the full User object from UserDetails

    /**
     * Constructs a WatchlistController with the necessary services.
     * @param watchlistItemService The service for watchlist operations.
     * @param userService The service for user-related operations.
     */
    public WatchlistController(WatchlistItemService watchlistItemService, UserService userService) {
        this.watchlistItemService = watchlistItemService;
        this.userService = userService;
    }

    /**
     * DTO (Data Transfer Object) for adding a movie to the watchlist.
     * This class is used to receive the request payload from the frontend.
     */
    static class AddWatchlistItemRequest {
        private int movieId;
        private String movieTitle;
        private String movieGenre;

        // Getters and Setters
        public int getMovieId() {
            return movieId;
        }

        public void setMovieId(int movieId) {
            this.movieId = movieId;
        }

        public String getMovieTitle() {
            return movieTitle;
        }

        public void setMovieTitle(String movieTitle) {
            this.movieTitle = movieTitle;
        }

        public String getMovieGenre() {
            return movieGenre;
        }

        public void setMovieGenre(String movieGenre) {
            this.movieGenre = movieGenre;
        }
    }

    /**
     * DTO for removing a movie from the watchlist.
     * Only the movieId is needed for removal.
     */
    static class RemoveWatchlistItemRequest {
        private int movieId;

        public int getMovieId() {
            return movieId;
        }

        public void setMovieId(int movieId) {
            this.movieId = movieId;
        }
    }

    /**
     * Adds a movie to the authenticated user's watchlist.
     * @param userDetails The authenticated user's details.
     * @param request The request body containing movie details.
     * @return ResponseEntity indicating success or if the movie is already in the watchlist.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addMovieToWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddWatchlistItemRequest request) {
        log.info("Attempting to add movie ID {} to watchlist for user: {}", request.getMovieId(), userDetails.getUsername());

        // Retrieve the full User object from the database using the authenticated user's email
        Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Authenticated user {} not found in database.", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        User user = userOptional.get();

        Optional<WatchlistItem> addedItem = watchlistItemService.addMovieToWatchlist(
                user,
                request.getMovieId(),
                request.getMovieTitle(),
                request.getMovieGenre()
        );

        if (addedItem.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(addedItem.get());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Movie already in watchlist.");
        }
    }

    /**
     * Removes a movie from the authenticated user's watchlist.
     * @param userDetails The authenticated user's details.
     * @param request The request body containing the movie ID to remove.
     * @return ResponseEntity indicating success or if the movie was not found in the watchlist.
     */
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeMovieFromWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody RemoveWatchlistItemRequest request) {
        log.info("Attempting to remove movie ID {} from watchlist for user: {}", request.getMovieId(), userDetails.getUsername());

        // Retrieve the full User object to get its ID
        Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Authenticated user {} not found in database.", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        User user = userOptional.get();

        boolean removed = watchlistItemService.removeMovieFromWatchlist(user.getId(), request.getMovieId());

        if (removed) {
            return ResponseEntity.ok("Movie removed from watchlist.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie not found in watchlist.");
        }
    }

    /**
     * Retrieves all watchlist items for the authenticated user.
     * @param userDetails The authenticated user's details.
     * @return ResponseEntity containing a list of WatchlistItem objects.
     */
    @GetMapping("/")
    public ResponseEntity<List<WatchlistItem>> getWatchlist(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching watchlist for user: {}", userDetails.getUsername());

        // Retrieve the full User object to get its ID
        Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Authenticated user {} not found in database for watchlist fetch.", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 with empty body
        }
        User user = userOptional.get();

        List<WatchlistItem> watchlist = watchlistItemService.getWatchlistByUserId(user.getId());
        return ResponseEntity.ok(watchlist);
    }

    /**
     * Checks if a specific movie is in the authenticated user's watchlist.
     * @param userDetails The authenticated user's details.
     * @param movieId The ID of the movie to check.
     * @return ResponseEntity with a boolean indicating presence and HTTP status.
     */
    @GetMapping("/check/{movieId}")
    public ResponseEntity<Boolean> checkMovieInWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int movieId) {
        log.info("Checking if movie ID {} is in watchlist for user: {}", movieId, userDetails.getUsername());

        Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Authenticated user {} not found in database for watchlist check.", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
        User user = userOptional.get();

        boolean isInWatchlist = watchlistItemService.isMovieInWatchlist(user.getId(), movieId);
        return ResponseEntity.ok(isInWatchlist);
    }
}
