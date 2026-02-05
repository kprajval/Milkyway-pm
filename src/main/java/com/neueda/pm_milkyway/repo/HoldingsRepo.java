package com.neueda.pm_milkyway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neueda.pm_milkyway.entity.HoldingsEntity;


@Repository
public interface HoldingsRepo extends JpaRepository<HoldingsEntity, Integer> {
    List<HoldingsEntity> findByStock(String stock);
}