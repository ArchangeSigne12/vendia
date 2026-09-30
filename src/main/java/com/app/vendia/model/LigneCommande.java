package com.app.vendia.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ligne_commande")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class LigneCommande {

    @EmbeddedId
    private LigneCommandeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProduit")
    @JoinColumn(name = "id_produit")
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCommande")
    @JoinColumn(name = "id_commande")
    private Commande commande;

    @Column(nullable = false)
    private Integer quantite;

    // Snapshot du prix unitaire au moment de la commande.
    // Ne JAMAIS le recalculer depuis Produit.prix a posteriori.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime date = LocalDateTime.now();
}
