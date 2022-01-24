package com.cqrs.query.repository;

import com.cqrs.query.entity.HolderAccountSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<HolderAccountSummary, String> {
    Optional<HolderAccountSummary> findByHolderId(String holderId);
}
