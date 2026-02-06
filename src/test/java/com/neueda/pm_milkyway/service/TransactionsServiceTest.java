package com.neueda.pm_milkyway.service;

import com.neueda.pm_milkyway.entity.HoldingsEntity;
import com.neueda.pm_milkyway.entity.TransactionEntity;
import com.neueda.pm_milkyway.repo.HoldingsRepo;
import com.neueda.pm_milkyway.repo.TransactionsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TransactionsServiceTest {

    @Mock
    private TransactionsRepo transactionsRepo;

    @Mock
    private HoldingsRepo holdingsRepo;

    @InjectMocks
    private TransactionsService transactionsService;

    private TransactionEntity testTransaction;
    private HoldingsEntity testHolding;

    @BeforeEach
    void setUp() {
        testTransaction = new TransactionEntity();
        testTransaction.setId(1);
        testTransaction.setDate(LocalDate.now());
        testTransaction.setType("BUY AAPL");
        testTransaction.setTransactionValue(BigDecimal.valueOf(1500.0));
        testTransaction.setPurseValue(BigDecimal.valueOf(98500.0));
        testTransaction.setStatus(true);

        testHolding = new HoldingsEntity();
        testHolding.setId(1);
        testHolding.setStock("AAPL");
        testHolding.setQuantity(10);
        testHolding.setTotal_invested(1500.0);
    }

    @Test
    void getAllTransactions_shouldReturnAllTransactions_whenTransactionsExist() {
        // Given: Multiple transactions in repository
        TransactionEntity transaction2 = new TransactionEntity();
        transaction2.setId(2);
        transaction2.setType("SELL GOOGL");
        transaction2.setPurseValue(BigDecimal.valueOf(99000.0));
        
        when(transactionsRepo.findAll()).thenReturn(Arrays.asList(testTransaction, transaction2));

        // When: Getting all transactions
        List<TransactionEntity> result = transactionsService.getAllTransactions();

        // Then: Should return all transactions
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transactionsRepo).findAll();
    }

    @Test
    void getPurseValue_shouldReturnDefaultValue_whenNoTransactionsExist() {
        // Given: Empty transaction repository
        when(transactionsRepo.findAll()).thenReturn(Collections.emptyList());

        // When: Getting purse value
        Double result = transactionsService.getPurseValue();

        // Then: Should return default starting capital
        assertNotNull(result);
        assertEquals(100000.0, result);
        verify(transactionsRepo).findAll();
    }

    @Test
    void getPurseValue_shouldReturnLatestPurseValue_whenTransactionsExist() {
        // Given: Transactions exist with latest purse value
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));

        // When: Getting purse value
        Double result = transactionsService.getPurseValue();

        // Then: Should return the latest purse value
        assertNotNull(result);
        assertEquals(98500.0, result);
        verify(transactionsRepo).findAll();
    }

    @Test
    void executePurchase_shouldThrowException_whenInsufficientFunds() {
        // Given: Low purse balance
        TransactionEntity lowBalanceTransaction = new TransactionEntity();
        lowBalanceTransaction.setId(1);
        lowBalanceTransaction.setPurseValue(BigDecimal.valueOf(500.0));
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(lowBalanceTransaction));

        // When & Then: Attempting to purchase should throw exception
        assertThrows(RuntimeException.class, () -> {
            transactionsService.executePurchase("AAPL", 10, 150.0);
        });
    }

    @Test
    void executeSale_shouldThrowException_whenStockNotOwned() {
        // Given: No holdings for the stock
        when(holdingsRepo.findByStock("TSLA")).thenReturn(Collections.emptyList());

        // When & Then: Attempting to sell should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.executeSale("TSLA", 5, 200.0);
        });
        
        assertEquals("You do not own this stock!", exception.getMessage());
        verify(holdingsRepo).findByStock("TSLA");
    }

    @Test
    void addToPurse_shouldCreateTransaction_whenAmountIsValid() {
        // Given: Valid purse add amount and current balance
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));
        when(transactionsRepo.save(any(TransactionEntity.class))).thenReturn(new TransactionEntity());

        // When: Adding to purse
        transactionsService.addToPurse(5000.0);

        // Then: Should save new transaction
        verify(transactionsRepo).findAll(); // Called once for getPurseValue
        verify(transactionsRepo).save(any(TransactionEntity.class));
    }

    @Test
    void deductFromPurse_shouldThrowException_whenInsufficientBalance() {
        // Given: Low purse balance
        TransactionEntity lowBalance = new TransactionEntity();
        lowBalance.setPurseValue(BigDecimal.valueOf(100.0));
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(lowBalance));

        // When & Then: Attempting to deduct more than available should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.deductFromPurse(500.0);
        });
        
        assertEquals("Insufficient purse balance", exception.getMessage());
    }

    @Test
    void executePurchase_shouldUpdateHoldings_whenNewStockPurchased() {
        // Given: Sufficient funds and no existing holdings
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));
        when(holdingsRepo.findByStock("TSLA")).thenReturn(Collections.emptyList());
        when(holdingsRepo.save(any(HoldingsEntity.class))).thenReturn(new HoldingsEntity());
        when(transactionsRepo.save(any(TransactionEntity.class))).thenReturn(new TransactionEntity());

        // When: Executing purchase
        transactionsService.executePurchase("TSLA", 5, 200.0);

        // Then: Should save new holding and transaction
        verify(holdingsRepo).findByStock("TSLA");
        verify(holdingsRepo).save(any(HoldingsEntity.class));
        verify(transactionsRepo).save(any(TransactionEntity.class));
    }

    @Test
    void executePurchase_shouldIncreaseQuantity_whenStockAlreadyOwned() {
        // Given: Existing holdings for stock
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Collections.singletonList(testHolding));
        when(holdingsRepo.save(any(HoldingsEntity.class))).thenReturn(testHolding);
        when(transactionsRepo.save(any(TransactionEntity.class))).thenReturn(new TransactionEntity());

        // When: Purchasing more of same stock
        transactionsService.executePurchase("AAPL", 5, 150.0);

        // Then: Should update existing holding
        verify(holdingsRepo).findByStock("AAPL");
        verify(holdingsRepo).save(any(HoldingsEntity.class));
        verify(transactionsRepo).save(any(TransactionEntity.class));
    }

    @Test
    void executeSale_shouldReduceQuantity_whenPartialSale() {
        // Given: Holdings with sufficient quantity
        testHolding.setQuantity(10);
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Collections.singletonList(testHolding));
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));
        when(holdingsRepo.save(any(HoldingsEntity.class))).thenReturn(testHolding);
        when(transactionsRepo.save(any(TransactionEntity.class))).thenReturn(new TransactionEntity());

        // When: Selling part of holdings
        transactionsService.executeSale("AAPL", 5, 160.0);

        // Then: Should update holding with reduced quantity
        verify(holdingsRepo).findByStock("AAPL");
        verify(holdingsRepo).save(any(HoldingsEntity.class));
        verify(transactionsRepo).save(any(TransactionEntity.class));
    }

    @Test
    void executeSale_shouldThrowException_whenInsufficientShares() {
        // Given: Holdings with less shares than requested
        testHolding.setQuantity(3);
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Collections.singletonList(testHolding));

        // When & Then: Attempting to sell more shares than owned
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.executeSale("AAPL", 10, 150.0);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient shares"));
    }

    @Test
    void handleAdjustment_shouldIncreaseQuantityByOne_whenActionIsPlus() {
        // Given: Existing holding and sufficient purse
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Collections.singletonList(testHolding));
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));
        when(holdingsRepo.save(any(HoldingsEntity.class))).thenReturn(testHolding);
        when(transactionsRepo.save(any(TransactionEntity.class))).thenReturn(new TransactionEntity());

        // When: Adjusting quantity up
        transactionsService.handleAdjustment("AAPL", "PLUS", 150.0);

        // Then: Should increase quantity by 1
        verify(holdingsRepo).findByStock("AAPL");
        verify(holdingsRepo).save(any(HoldingsEntity.class));
        verify(transactionsRepo).save(any(TransactionEntity.class));
    }

    @Test
    void handleAdjustment_shouldDecreaseQuantityByOne_whenActionIsMinus() {
        // Given: Existing holding with quantity > 1
        testHolding.setQuantity(5);
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Collections.singletonList(testHolding));
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));
        when(holdingsRepo.save(any(HoldingsEntity.class))).thenReturn(testHolding);
        when(transactionsRepo.save(any(TransactionEntity.class))).thenReturn(new TransactionEntity());

        // When: Adjusting quantity down
        transactionsService.handleAdjustment("AAPL", "MINUS", 150.0);

        // Then: Should decrease quantity by 1
        verify(holdingsRepo).findByStock("AAPL");
        verify(transactionsRepo).save(any(TransactionEntity.class));
    }

    @Test
    void handleAdjustment_shouldThrowException_whenInsufficientFundsForPlus() {
        // Given: Low purse balance
        TransactionEntity lowBalance = new TransactionEntity();
        lowBalance.setPurseValue(BigDecimal.valueOf(50.0));
        when(holdingsRepo.findByStock("AAPL")).thenReturn(Collections.singletonList(testHolding));
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(lowBalance));

        // When & Then: Attempting to add with insufficient funds
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.handleAdjustment("AAPL", "PLUS", 150.0);
        });
        
        assertEquals("Insufficient Funds", exception.getMessage());
    }

    @Test
    void handleAdjustment_shouldDoNothing_whenHoldingNotFound() {
        // Given: No existing holding
        when(holdingsRepo.findByStock("TSLA")).thenReturn(Collections.emptyList());

        // When: Attempting to adjust non-existent holding
        transactionsService.handleAdjustment("TSLA", "PLUS", 150.0);

        // Then: Should not save anything
        verify(holdingsRepo).findByStock("TSLA");
        verify(holdingsRepo, never()).save(any());
        verify(transactionsRepo, never()).save(any());
    }

    @Test
    void getPortfolioStats_shouldCalculateCorrectly_whenHoldingsExist() {
        // Given: Holdings and current prices
        List<HoldingsEntity> holdings = Arrays.asList(testHolding);
        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 160.0);
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));

        // When: Getting portfolio stats
        Map<String, Object> stats = transactionsService.getPortfolioStats(holdings, prices);

        // Then: Should calculate all values correctly
        assertNotNull(stats);
        assertTrue(stats.containsKey("purse"));
        assertTrue(stats.containsKey("portfolioValue"));
        assertTrue(stats.containsKey("profitLoss"));
        assertTrue(stats.containsKey("changePercent"));
    }

    @Test
    void getPortfolioStats_shouldHandleEmptyHoldings_correctly() {
        // Given: No holdings
        List<HoldingsEntity> emptyHoldings = Collections.emptyList();
        Map<String, Double> prices = new HashMap<>();
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));

        // When: Getting stats with no holdings
        Map<String, Object> stats = transactionsService.getPortfolioStats(emptyHoldings, prices);

        // Then: Should return zero values except purse
        assertNotNull(stats);
        assertEquals(98500.0, stats.get("purse"));
        assertEquals(0.0, ((Number) stats.get("profitLoss")).doubleValue());
    }

    @Test
    void addToPurse_shouldThrowException_whenAmountIsZero() {
        // Given: Zero amount
        
        // When & Then: Adding zero should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.addToPurse(0.0);
        });
        
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void addToPurse_shouldThrowException_whenAmountIsNegative() {
        // Given: Negative amount
        
        // When & Then: Adding negative amount should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.addToPurse(-100.0);
        });
        
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void deductFromPurse_shouldThrowException_whenAmountIsZero() {
        // Given: Zero amount
        
        // When & Then: Deducting zero should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.deductFromPurse(0.0);
        });
        
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void deductFromPurse_shouldThrowException_whenAmountIsNegative() {
        // Given: Negative amount
        
        // When & Then: Deducting negative amount should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.deductFromPurse(-50.0);
        });
        
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void deductFromPurse_shouldCreateTransaction_whenSufficientBalance() {
        // Given: Sufficient balance
        when(transactionsRepo.findAll()).thenReturn(Collections.singletonList(testTransaction));
        when(transactionsRepo.save(any(TransactionEntity.class))).thenReturn(new TransactionEntity());

        // When: Deducting valid amount
        transactionsService.deductFromPurse(1000.0);

        // Then: Should save transaction
        verify(transactionsRepo).findAll();
        verify(transactionsRepo).save(any(TransactionEntity.class));
    }
}
