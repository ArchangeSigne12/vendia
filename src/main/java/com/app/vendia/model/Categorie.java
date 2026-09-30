package com.app.vendia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorie")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;
}
