package com.app.vendia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "panier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Panier {                 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_panier")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false, unique = true)
    private Client client;

    @Column(name = "date_creation", nullable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LignePanier> lignes = new ArrayList<>();
}