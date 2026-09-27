package com.app.vendia.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiement")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commande", nullable = false)
    private Commande commande;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "reference_transaction")
    private String referenceTransaction; // id du PaymentIntent Stripe

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    private String methode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;
}
