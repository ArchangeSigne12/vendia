package com.app.vendia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_note")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @Column(nullable = false)
    private Short note; // 1 à 5

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
