package com.app.vendia.service;

import com.app.vendia.dto.response.AdminCommandeResponse;
import com.app.vendia.exception.RessourceIntrouvableException;
import com.app.vendia.exception.TransitionStatutInvalideException;
import com.app.vendia.model.Commande;
import com.app.vendia.model.LigneCommande;
import com.app.vendia.model.Produit;
import com.app.vendia.model.StatutCommande;
import com.app.vendia.repository.CommandeRepository;
import com.app.vendia.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class AdminCommandeService {

    private static final Map<StatutCommande, Set<StatutCommande>> TRANSITIONS_AUTORISEES =
            new EnumMap<>(StatutCommande.class);
    static {
        TRANSITIONS_AUTORISEES.put(StatutCommande.EN_ATTENTE, EnumSet.of(StatutCommande.ANNULEE));
        TRANSITIONS_AUTORISEES.put(StatutCommande.PAYEE, EnumSet.of(StatutCommande.EN_PREPARATION, StatutCommande.ANNULEE));
        TRANSITIONS_AUTORISEES.put(StatutCommande.EN_PREPARATION, EnumSet.of(StatutCommande.EXPEDIEE, StatutCommande.ANNULEE));
        TRANSITIONS_AUTORISEES.put(StatutCommande.EXPEDIEE, EnumSet.of(StatutCommande.LIVREE));
        TRANSITIONS_AUTORISEES.put(StatutCommande.LIVREE, EnumSet.noneOf(StatutCommande.class));
        TRANSITIONS_AUTORISEES.put(StatutCommande.ANNULEE, EnumSet.noneOf(StatutCommande.class));
    }

   
    private static final Set<StatutCommande> STATUTS_STOCK_DECREMENTE =
            EnumSet.of(StatutCommande.PAYEE, StatutCommande.EN_PREPARATION, StatutCommande.EXPEDIEE);

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;

    @Transactional(readOnly = true)
    public Page<AdminCommandeResponse> lister(StatutCommande statut, Pageable pageable) {
        Page<Commande> page = (statut != null)
                ? commandeRepository.findByStatut(statut, pageable)
                : commandeRepository.findAll(pageable);
        return page.map(this::versDto);
    }

    @Transactional(readOnly = true)
    public AdminCommandeResponse detail(Long commandeId) {
        return versDto(trouver(commandeId));
    }

    @Transactional
    public AdminCommandeResponse changerStatut(Long commandeId, StatutCommande cible) {
        Commande commande = trouver(commandeId);
        StatutCommande actuel = commande.getStatut();

        if (!TRANSITIONS_AUTORISEES.getOrDefault(actuel, Set.of()).contains(cible)) {
            throw new TransitionStatutInvalideException(actuel, cible);
        }

        if (cible == StatutCommande.ANNULEE && STATUTS_STOCK_DECREMENTE.contains(actuel)) {
            restituerStock(commande);
        }

        commande.setStatut(cible);
        return versDto(commande);
    }

    private void restituerStock(Commande commande) {
        for (LigneCommande ligne : commande.getLignes()) {
            Produit produit = produitRepository.findById(ligne.getProduit().getId())
                    .orElseThrow(() -> new RessourceIntrouvableException("Produit", ligne.getProduit().getId()));
            produit.setStock(produit.getStock() + ligne.getQuantite()); // @Version protège l'accès concurrent
        }
    }

    private Commande trouver(Long commandeId) {
        return commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Commande", commandeId));
    }

    private AdminCommandeResponse versDto(Commande c) {
        int nombreArticles = c.getLignes().stream().mapToInt(LigneCommande::getQuantite).sum();
        BigDecimal total = c.getLignes().stream()
                .map(l -> l.getPrix().multiply(BigDecimal.valueOf(l.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminCommandeResponse(
                c.getId(), c.getClient().getId(),
                c.getClient().getNom() + " " + c.getClient().getPrenom(),
                c.getClient().getEmail(),
                c.getDateCreation(), c.getStatut(), nombreArticles, total);
    }
}
