package com.cargopro.LoaderBooker.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cargopro.LoaderBooker.model.entity.Load;

@Repository
public interface LoadRepository extends JpaRepository<Load,Long> {

    Optional<Load> findById(UUID loadId);

    public boolean existsById(UUID loadId);

    public void deleteById(UUID loadId);

}
