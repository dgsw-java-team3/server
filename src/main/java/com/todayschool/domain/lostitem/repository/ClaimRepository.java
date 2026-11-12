package com.todayschool.domain.lostitem.repository;

import com.todayschool.domain.lostitem.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
}
