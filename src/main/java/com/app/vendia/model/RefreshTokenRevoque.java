package com.app.vendia.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_token_revoque")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class RefreshTokenRevoque {

    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @Column(name = "date_revocation", nullable = false)
    private LocalDateTime dateRevocation = LocalDateTime.now();

    public RefreshTokenRevoque(String tokenHash) {
        this.tokenHash = tokenHash;
    }
}