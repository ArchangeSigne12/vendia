package com.app.vendia.repository;

import com.app.vendia.model.TokenRevoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRevoqueRepository extends JpaRepository<TokenRevoque, String> {
    
}
