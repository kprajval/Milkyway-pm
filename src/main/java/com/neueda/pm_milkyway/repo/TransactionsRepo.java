package com.neueda.pm_milkyway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neueda.pm_milkyway.entity.TransactionEntity;

@Repository
public interface TransactionsRepo extends JpaRepository<TransactionEntity, Integer> {

}