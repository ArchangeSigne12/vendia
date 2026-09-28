package com.app.vendia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ligne_panier")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LignePanier {

    @EmbeddedId
    private PanierLigneId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPanier")
    @JoinColumn(name = "id_panier")
    private Panier panier;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProduit")
    @JoinColumn(name = "id_produit")
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;
}
