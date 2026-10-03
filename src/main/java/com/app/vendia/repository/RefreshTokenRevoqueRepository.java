package com.app.vendia.repository;

import com.app.vendia.model.RefreshTokenRevoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRevoqueRepository extends JpaRepository<RefreshTokenRevoque, String> {
    
}
