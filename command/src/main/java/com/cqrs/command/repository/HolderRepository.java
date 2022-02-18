package com.cqrs.command.repository;

import com.cqrs.command.aggregate.HolderAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HolderRepository extends JpaRepository<HolderAggregate, String> {
    Optional<HolderAggregate> findHolderAggregateByHolderID(String id);
}
