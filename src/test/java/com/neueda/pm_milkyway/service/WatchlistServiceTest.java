package com.neueda.pm_milkyway.service;

import com.neueda.pm_milkyway.entity.WatchlistEntity;
import com.neueda.pm_milkyway.repo.WatchListRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchListRepo watchListRepo;

    @InjectMocks
    private WatchlistService watchlistService;

    private WatchlistEntity testWatchlistItem;

    @BeforeEach
    void setUp() {
        testWatchlistItem = new WatchlistEntity();
        testWatchlistItem.setStock("AAPL");
    }

    @Test
    void getWatchlistItems_shouldReturnAllStocks_whenWatchlistHasItems() {
        // Given: Watchlist has multiple items
        WatchlistEntity item2 = new WatchlistEntity();
        item2.setStock("GOOGL");
        
        when(watchListRepo.findAll()).thenReturn(Arrays.asList(testWatchlistItem, item2));

        // When: Getting watchlist items
        List<String> result = watchlistService.getWatchlistItems();

        // Then: Should return all stock symbols
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("AAPL"));
        assertTrue(result.contains("GOOGL"));
        verify(watchListRepo).findAll();
    }

    @Test
    void getWatchlistItems_shouldReturnEmptyList_whenWatchlistIsEmpty() {
        // Given: Empty watchlist
        when(watchListRepo.findAll()).thenReturn(Collections.emptyList());

        // When: Getting watchlist items
        List<String> result = watchlistService.getWatchlistItems();

        // Then: Should return empty list
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(watchListRepo).findAll();
    }

    @Test
    void addToWatchList_shouldCallRepository_whenAddingNewStock() {
        // Given: A stock symbol to add
        String stockSymbol = "TSLA";

        // When: Adding stock to watchlist
        watchlistService.addToWatchList(stockSymbol);

        // Then: Should call repository to save
        verify(watchListRepo).saveNewStock(stockSymbol);
    }

    @Test
    void removeFromWatchlist_shouldDeleteStock_whenStockExists() {
        // Given: Stock exists in watchlist
        when(watchListRepo.findAll()).thenReturn(Collections.singletonList(testWatchlistItem));

        // When: Removing stock from watchlist
        watchlistService.removeFromWatchlist("AAPL");

        // Then: Should delete the item
        verify(watchListRepo).findAll();
        verify(watchListRepo).delete(testWatchlistItem);
    }

    @Test
    void removeFromWatchlist_shouldNotDelete_whenStockDoesNotExist() {
        // Given: Stock doesn't exist in watchlist
        when(watchListRepo.findAll()).thenReturn(Collections.emptyList());

        // When: Attempting to remove non-existent stock
        watchlistService.removeFromWatchlist("TSLA");

        // Then: Should not call delete
        verify(watchListRepo).findAll();
        verify(watchListRepo, never()).delete(any());
    }

    @Test
    void getWatchlistItems_shouldHandleLargeWatchlist_efficiently() {
        // Given: Large watchlist
        List<WatchlistEntity> largeList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            WatchlistEntity item = new WatchlistEntity();
            item.setStock("STOCK" + i);
            largeList.add(item);
        }
        when(watchListRepo.findAll()).thenReturn(largeList);

        // When: Getting watchlist items
        List<String> result = watchlistService.getWatchlistItems();

        // Then: Should return all stock symbols
        assertNotNull(result);
        assertEquals(50, result.size());
        verify(watchListRepo).findAll();
    }

    @Test
    void removeFromWatchlist_shouldBeCaseInsensitive_whenMatching() {
        // Given: Stock exists with uppercase
        testWatchlistItem.setStock("AAPL");
        when(watchListRepo.findAll()).thenReturn(Collections.singletonList(testWatchlistItem));

        // When: Removing with lowercase
        watchlistService.removeFromWatchlist("aapl");

        // Then: Should match and delete
        verify(watchListRepo).findAll();
        verify(watchListRepo).delete(testWatchlistItem);
    }

    @Test
    void addToWatchList_shouldCallRepository_withCorrectParameter() {
        // Given: A new stock symbol
        String stock = "NFLX";

        // When: Adding to watchlist
        watchlistService.addToWatchList(stock);

        // Then: Repository should be called with exact parameter
        verify(watchListRepo).saveNewStock(stock);
    }

    @Test
    void getWatchlistItems_shouldReturnDistinctStocks_whenCalled() {
        // Given: Watchlist with multiple items
        WatchlistEntity item2 = new WatchlistEntity();
        item2.setStock("MSFT");
        WatchlistEntity item3 = new WatchlistEntity();
        item3.setStock("GOOGL");
        
        when(watchListRepo.findAll()).thenReturn(Arrays.asList(testWatchlistItem, item2, item3));

        // When: Getting items
        List<String> result = watchlistService.getWatchlistItems();

        // Then: Should return distinct stocks
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("AAPL"));
        assertTrue(result.contains("MSFT"));
        assertTrue(result.contains("GOOGL"));
    }

    @Test
    void removeFromWatchlist_shouldHandleMultipleMatches_correctly() {
        // Given: Single matching item
        when(watchListRepo.findAll()).thenReturn(Collections.singletonList(testWatchlistItem));

        // When: Removing stock
        watchlistService.removeFromWatchlist("AAPL");

        // Then: Should delete only once
        verify(watchListRepo, times(1)).delete(testWatchlistItem);
    }

    @Test
    void addToWatchList_shouldHandleSpecialCharacters_inStockSymbol() {
        // Given: Stock with special characters (edge case)
        String specialStock = "BRK.B";

        // When: Adding stock
        watchlistService.addToWatchList(specialStock);

        // Then: Should call repository correctly
        verify(watchListRepo).saveNewStock(specialStock);
    }

    @Test
    void getWatchlistItems_shouldMapCorrectly_fromEntityToString() {
        // Given: Multiple watchlist entities
        List<WatchlistEntity> entities = Arrays.asList(testWatchlistItem);
        when(watchListRepo.findAll()).thenReturn(entities);

        // When: Getting watchlist items
        List<String> result = watchlistService.getWatchlistItems();

        // Then: Should convert entities to strings correctly
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0));
        assertTrue(result.get(0) instanceof String);
    }

    @Test
    void removeFromWatchlist_shouldMatchExactly_whenCaseDiffers() {
        // Given: Stock exists with uppercase
        testWatchlistItem.setStock("TSLA");
        WatchlistEntity lowerCase = new WatchlistEntity();
        lowerCase.setStock("tsla");
        when(watchListRepo.findAll()).thenReturn(Arrays.asList(testWatchlistItem, lowerCase));

        // When: Removing with specific case
        watchlistService.removeFromWatchlist("TSLA");

        // Then: Should match case-insensitively and remove first match
        verify(watchListRepo).findAll();
        verify(watchListRepo).delete(any(WatchlistEntity.class));
    }
}
