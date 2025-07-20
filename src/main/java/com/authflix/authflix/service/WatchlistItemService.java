package com.authflix.authflix.service;

import com.authflix.authflix.model.User;
import com.authflix.authflix.model.WatchlistItem;
import com.authflix.authflix.repository.WatchlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing WatchlistItem operations.
 * This class provides methods to add, remove, and retrieve movies from a user's watchlist.
 */
@Service
public class WatchlistItemService {

    private static final Logger log = LoggerFactory.getLogger(WatchlistItemService.class);

    private final WatchlistItemRepository watchlistItemRepository;

    /**
     * Constructs a WatchlistItemService with the given WatchlistItemRepository.
     * @param watchlistItemRepository The repository for accessing watchlist item data.
     */
    public WatchlistItemService(WatchlistItemRepository watchlistItemRepository) {
        this.watchlistItemRepository = watchlistItemRepository;
    }

    /**
     * Adds a movie to a user's watchlist.
     * Checks if the movie is already in the watchlist before adding.
     * @param user The user who is adding the movie.
     * @param movieId The ID of the movie to add (from FastAPI service).
     * @param movieTitle The title of the movie.
     * @param movieGenre The genre of the movie.
     * @return An Optional containing the newly added WatchlistItem if successful, or empty if already exists.
     */
    @Transactional
    public Optional<WatchlistItem> addMovieToWatchlist(User user, int movieId, String movieTitle, String movieGenre) {
        // Check if the movie is already in the user's watchlist
        Optional<WatchlistItem> existingItem = watchlistItemRepository.findByUserIdAndMovieId(user.getId(), movieId);
        if (existingItem.isPresent()) {
            log.info("Movie ID {} is already in user {}'s watchlist.", movieId, user.getEmail());
            return Optional.empty(); // Movie already exists in watchlist
        }

        WatchlistItem newItem = WatchlistItem.builder()
                .user(user)
                .movieId(movieId)
                .movieTitle(movieTitle)
                .movieGenre(movieGenre)
                .build();
        watchlistItemRepository.save(newItem);
        log.info("Movie ID {} added to user {}'s watchlist.", movieId, user.getEmail());
        return Optional.of(newItem);
    }

    /**
     * Removes a movie from a user's watchlist.
     * @param userId The ID of the user.
     * @param movieId The ID of the movie to remove.
     * @return true if the item was found and deleted, false otherwise.
     */
    @Transactional
    public boolean removeMovieFromWatchlist(Long userId, int movieId) {
        Optional<WatchlistItem> itemToDelete = watchlistItemRepository.findByUserIdAndMovieId(userId, movieId);
        if (itemToDelete.isPresent()) {
            watchlistItemRepository.delete(itemToDelete.get());
            log.info("Movie ID {} removed from user {}'s watchlist.", movieId, userId);
            return true;
        }
        log.info("Movie ID {} not found in user {}'s watchlist for removal.", movieId, userId);
        return false;
    }

    /**
     * Retrieves all watchlist items for a specific user.
     * @param userId The ID of the user.
     * @return A list of WatchlistItem objects.
     */
    public List<WatchlistItem> getWatchlistByUserId(Long userId) {
        log.info("Fetching watchlist for user ID: {}.", userId);
        return watchlistItemRepository.findByUserId(userId);
    }

    /**
     * Checks if a specific movie is in a user's watchlist.
     * @param userId The ID of the user.
     * @param movieId The ID of the movie.
     * @return true if the movie is in the watchlist, false otherwise.
     */
    public boolean isMovieInWatchlist(Long userId, int movieId) {
        return watchlistItemRepository.findByUserIdAndMovieId(userId, movieId).isPresent();
    }
}
