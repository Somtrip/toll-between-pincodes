package com.som.toll.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.som.toll.entity.TollPlaza;

public interface TollPlazaRepository extends JpaRepository<TollPlaza, Long> {
}

