package com.som.toll.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.som.toll.entity.TollRouteCache;

import java.util.Optional;

public interface TollRouteCacheRepository extends JpaRepository<TollRouteCache, Long> {
    Optional<TollRouteCache> findBySourcePincodeAndDestinationPincode(String source, String destination);
}

