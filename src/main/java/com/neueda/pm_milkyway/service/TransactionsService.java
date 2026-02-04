package com.neueda.pm_milkyway.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neueda.pm_milkyway.entity.TransactionEntity;
import com.neueda.pm_milkyway.repo.TransactionsRepo;

@Service
public class TransactionsService {

    private final TransactionsRepo transactionsRepo;

    @Autowired
    public TransactionsService(TransactionsRepo transactionsRepo) {
        this.transactionsRepo = transactionsRepo;
    }

    public List<TransactionEntity> getAllTransactions() {
        return transactionsRepo.findAll();
    }

    public Double getPurseValue() {
        Double lastValue = 0.0;
        String query = "SELECT purseValue FROM transactions ORDER BY id DESC LIMIT 1";

        String DB_URL = "jdbc:mysql://localhost:3306/milkyway_pm";
        String USER = "root";
        String PASS = "root";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                lastValue = rs.getDouble("purseValue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return lastValue;
    }
}
