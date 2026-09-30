package com.app.vendia.repository;

import com.app.vendia.model.Commande;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    Page<Commande> findByClientIdOrderByDateCreationDesc(Long clientId, Pageable pageable);

    Optional<Commande> findByIdAndClientId(Long id, Long clientId);

    Optional<Commande> findByIdAndClientId(long id, long clientId);
}