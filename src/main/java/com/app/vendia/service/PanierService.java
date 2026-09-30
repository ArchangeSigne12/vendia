package com.app.vendia.service;

import com.app.vendia.model.*;
import com.app.vendia.model.Produit;
import com.app.vendia.dto.response.LignePanierResponse;
import com.app.vendia.dto.response.PanierResponse;
import com.app.vendia.exception.RessourceIntrouvableException;
import com.app.vendia.exception.StockInsuffisantException;
import com.app.vendia.repository.ClientRepository;
import com.app.vendia.repository.PanierRepository;
import com.app.vendia.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PanierService {

    private final PanierRepository panierRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;

    @Transactional
    public PanierResponse voirPanier(Long clientId) {
        return versDto(obtenirOuCreerPanier(clientId));
    }

    @Transactional
    public PanierResponse ajouterLigne(Long clientId, Long produitId, int quantite) {
        Panier panier = obtenirOuCreerPanier(clientId);
        Produit produit = trouverProduit(produitId);

        Optional<LignePanier> ligneExistante = panier.getLignes().stream()
                .filter(l -> l.getProduit().getId().equals(produitId))
                .findFirst();

        int quantiteCible = ligneExistante.map(LignePanier::getQuantite).orElse(0) + quantite;
        verifierStockDisponible(produit, quantiteCible);

        if (ligneExistante.isPresent()) {
            ligneExistante.get().setQuantite(quantiteCible);
        } else {
            LignePanier ligne = LignePanier.builder()
                    .id(new PanierLigneId(panier.getId(), produitId))
                    .panier(panier)
                    .produit(produit)
                    .quantite(quantite)
                    .build();
            panier.getLignes().add(ligne);
        }

        return versDto(panier);
    }

    @Transactional
    public PanierResponse modifierQuantite(Long clientId, Long produitId, int quantite) {
        Panier panier = obtenirOuCreerPanier(clientId);
        LignePanier ligne = trouverLigne(panier, produitId);

        verifierStockDisponible(ligne.getProduit(), quantite);
        ligne.setQuantite(quantite);

        return versDto(panier);
    }

    @Transactional
    public PanierResponse supprimerLigne(Long clientId, Long produitId) {
        Panier panier = obtenirOuCreerPanier(clientId);
        LignePanier ligne = trouverLigne(panier, produitId);
        panier.getLignes().remove(ligne);
        return versDto(panier);
    }

    @Transactional
    public void viderPanier(Long clientId) {
        Panier panier = obtenirOuCreerPanier(clientId);
        panier.getLignes().clear();
    }

    // --- Aides internes ---------------------------------------------------

    private Panier obtenirOuCreerPanier(Long clientId) {
        return panierRepository.findByClientId(clientId)
                .orElseGet(() -> {
                    Client client = clientRepository.findById(clientId)
                            .orElseThrow(() -> new RessourceIntrouvableException("Client", clientId));
                    return panierRepository.save(Panier.builder().client(client).build());
                });
    }

    private Produit trouverProduit(Long produitId) {
        return produitRepository.findById(produitId)
                .orElseThrow(() -> new RessourceIntrouvableException("Produit", produitId));
    }

    private LignePanier trouverLigne(Panier panier, Long produitId) {
        return panier.getLignes().stream()
                .filter(l -> l.getProduit().getId().equals(produitId))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("LignePanier", produitId));
    }

    private void verifierStockDisponible(Produit produit, int quantiteDemandee) {
        if (produit.getStock() < quantiteDemandee) {
            throw new StockInsuffisantException(produit.getNom(), produit.getStock(), quantiteDemandee);
        }
    }

    private PanierResponse versDto(Panier panier) {
        var lignes = panier.getLignes().stream()
                .map(l -> {
                    Produit p = l.getProduit();
                    BigDecimal sousTotal = p.getPrix().multiply(BigDecimal.valueOf(l.getQuantite()));
                    return new LignePanierResponse(
                            p.getId(), p.getNom(), p.getImage(), p.getPrix(),
                            l.getQuantite(), sousTotal, p.getStock() >= l.getQuantite()
                    );
                })
                .toList();

        BigDecimal total = lignes.stream()
                .map(LignePanierResponse::sousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PanierResponse(panier.getId(), lignes, total);
    }
}
