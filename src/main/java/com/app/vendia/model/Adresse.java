package com.app.vendia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "adresse")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adresse")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client")
    private Client client;

    private String intitule;

    @Column(name = "ligne_1", nullable = false)
    private String ligne1;

    @Column(name = "ligne_2")
    private String ligne2;

    @Column(name = "code_postal", nullable = false, length = 20)
    private String codePostal;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column(nullable = false, length = 100)
    private String pays;
}
