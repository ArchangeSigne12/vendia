package com.app.vendia.repository;

import com.app.vendia.model.Panier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PanierRepository extends JpaRepository<Panier, Long> {
    
    Optional<Panier> findByClientId(Long clientId);
}
