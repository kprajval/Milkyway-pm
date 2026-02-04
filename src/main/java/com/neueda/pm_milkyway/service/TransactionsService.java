package com.neueda.pm_milkyway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neueda.pm_milkyway.repo.TransactionsRepo;

@Service
public class TransactionsService {

    private final TransactionsRepo transactionsRepo;

    @Autowired
    public TransactionsService(TransactionsRepo transactionsRepo) {
        this.transactionsRepo = transactionsRepo;
    }
}
