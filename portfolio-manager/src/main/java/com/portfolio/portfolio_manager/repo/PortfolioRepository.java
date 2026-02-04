package com.portfolio.portfolio_manager.repo;

import com.portfolio.portfolio_manager.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
