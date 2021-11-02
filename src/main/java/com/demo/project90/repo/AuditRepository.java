package com.demo.project90.repo;

import java.util.Optional;

import com.demo.project90.domain.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long> {
    Optional<Audit> findByToken(String token);
}
