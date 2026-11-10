package com.todayschool.lostitem.repository;

import com.todayschool.lostitem.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
}
