package com.app.vendia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_revoque")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class TokenRevoque {

    @Id
    @Column(name = "jti", length = 100)
    private String jti;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;
}
