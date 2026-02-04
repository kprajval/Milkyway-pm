package com.neueda.pm_milkyway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transaction;

@Repository
public interface TransactionsRepo extends JpaRepository<Transaction, Integer> {

}