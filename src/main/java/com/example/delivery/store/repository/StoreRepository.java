package com.example.delivery.store.repository;

import com.example.delivery.store.entity.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByOwner_Id(Long ownerId);

    Optional<Store> findByOwner_Id(Long ownerId);
}
