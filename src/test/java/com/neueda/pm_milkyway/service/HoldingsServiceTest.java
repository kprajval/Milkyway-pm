package com.neueda.pm_milkyway.service;

import com.neueda.pm_milkyway.entity.HoldingsEntity;
import com.neueda.pm_milkyway.repo.HoldingsRepo;
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

@ExtendWith(MockitoExtension.class)
class HoldingsServiceTest {

    @Mock
    private HoldingsRepo holdingsRepo;

    @InjectMocks
    private HoldingsService holdingsService;

    private HoldingsEntity testHolding;

    @BeforeEach
    void setUp() {
        testHolding = new HoldingsEntity();
        testHolding.setId(1);
        testHolding.setStock("AAPL");
        testHolding.setQuantity(10);
        testHolding.setTotal_invested(1500.0);
    }

    @Test
    void getAllHoldings_shouldReturnAllHoldings_whenHoldingsExist() {
        // Given: Multiple holdings in the repository
        HoldingsEntity holding2 = new HoldingsEntity();
        holding2.setId(2);
        holding2.setStock("GOOGL");
        holding2.setQuantity(5);
        holding2.setTotal_invested(750.0);
        
        when(holdingsRepo.findAll()).thenReturn(Arrays.asList(testHolding, holding2));

        // When: Getting all holdings
        List<HoldingsEntity> result = holdingsService.getAllHoldings();

        // Then: Should return all holdings
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getStock());
        assertEquals("GOOGL", result.get(1).getStock());
        verify(holdingsRepo).findAll();
    }

    @Test
    void getAllHoldings_shouldReturnEmptyList_whenNoHoldingsExist() {
        // Given: Empty repository
        when(holdingsRepo.findAll()).thenReturn(Collections.emptyList());

        // When: Getting all holdings
        List<HoldingsEntity> result = holdingsService.getAllHoldings();

        // Then: Should return empty list
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(holdingsRepo).findAll();
    }

    @Test
    void getStockHoldings_shouldReturnSpecificStock_whenStockExists() {
        // Given: Repository has specific stock
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Collections.singletonList(testHolding));

        // When: Getting holdings for specific stock
        List<HoldingsEntity> result = holdingsService.getStockHoldings("AAPL");

        // Then: Should return only that stock
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getStock());
        assertEquals(10, result.get(0).getQuantity());
        verify(holdingsRepo).findByStock("AAPL");
    }

    @Test
    void getStockHoldings_shouldReturnEmptyList_whenStockNotFound() {
        // Given: Stock doesn't exist in repository
        when(holdingsRepo.findByStock("UNKNOWN")).thenReturn(Collections.emptyList());

        // When: Getting holdings for non-existent stock
        List<HoldingsEntity> result = holdingsService.getStockHoldings("UNKNOWN");

        // Then: Should return empty list
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(holdingsRepo).findByStock("UNKNOWN");
    }

    @Test
    void getStockHoldings_shouldReturnMultipleEntries_whenDuplicatesExist() {
        // Given: Multiple entries for same stock (edge case)
        HoldingsEntity duplicate = new HoldingsEntity();
        duplicate.setId(3);
        duplicate.setStock("AAPL");
        duplicate.setQuantity(5);
        duplicate.setTotal_invested(800.0);
        
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Arrays.asList(testHolding, duplicate));

        // When: Getting holdings
        List<HoldingsEntity> result = holdingsService.getStockHoldings("AAPL");

        // Then: Should return all entries
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(holdingsRepo).findByStock("AAPL");
    }

    @Test
    void getAllHoldings_shouldReturnHoldingsInOrder_whenMultipleExist() {
        // Given: Multiple holdings with different values
        HoldingsEntity holding2 = new HoldingsEntity();
        holding2.setId(2);
        holding2.setStock("MSFT");
        holding2.setQuantity(8);
        holding2.setTotal_invested(2400.0);

        HoldingsEntity holding3 = new HoldingsEntity();
        holding3.setId(3);
        holding3.setStock("TSLA");
        holding3.setQuantity(3);
        holding3.setTotal_invested(600.0);
        
        when(holdingsRepo.findAll()).thenReturn(Arrays.asList(testHolding, holding2, holding3));

        // When: Getting all holdings
        List<HoldingsEntity> result = holdingsService.getAllHoldings();

        // Then: Should return all in order
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("AAPL", result.get(0).getStock());
        assertEquals("MSFT", result.get(1).getStock());
        assertEquals("TSLA", result.get(2).getStock());
    }

    @Test
    void getStockHoldings_shouldHandleCaseVariations_whenSearching() {
        // Given: Stock exists with specific case
        when(holdingsRepo.findByStock("aapl")).thenReturn(Collections.emptyList());

        // When: Searching with different case
        List<HoldingsEntity> result = holdingsService.getStockHoldings("aapl");

        // Then: Repository method should be called with exact parameter
        verify(holdingsRepo).findByStock("aapl");
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllHoldings_shouldHandleLargeDataset_efficiently() {
        // Given: Large number of holdings
        List<HoldingsEntity> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            HoldingsEntity holding = new HoldingsEntity();
            holding.setId(i);
            holding.setStock("STOCK" + i);
            holding.setQuantity(i * 10);
            holding.setTotal_invested(i * 1000.0);
            largeList.add(holding);
        }
        when(holdingsRepo.findAll()).thenReturn(largeList);

        // When: Getting all holdings
        List<HoldingsEntity> result = holdingsService.getAllHoldings();

        // Then: Should return all holdings
        assertNotNull(result);
        assertEquals(100, result.size());
        verify(holdingsRepo).findAll();
    }

    @Test
    void getStockHoldings_shouldReturnValidData_whenStockHasZeroQuantity() {
        // Given: Holding with zero quantity (edge case)
        testHolding.setQuantity(0);
        testHolding.setTotal_invested(0.0);
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Collections.singletonList(testHolding));

        // When: Getting holdings
        List<HoldingsEntity> result = holdingsService.getStockHoldings("AAPL");

        // Then: Should still return the holding
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getQuantity());
    }
}
