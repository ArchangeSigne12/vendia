package com.app.vendia.service;

import com.app.vendia.dto.request.AdresseRequest;
import com.app.vendia.dto.request.CommandeRequest;
import com.app.vendia.dto.response.AdresseResponse;
import com.app.vendia.dto.response.CommandeResponse;
import com.app.vendia.dto.response.CommandeResumeResponse;
import com.app.vendia.dto.response.LigneCommandeResponse;
import com.app.vendia.exception.CommandeNonAnnulableException;
import com.app.vendia.exception.PanierVideException;
import com.app.vendia.exception.RessourceIntrouvableException;
import com.app.vendia.exception.StockInsuffisantException;
import com.app.vendia.model.*;
import com.app.vendia.repository.AdresseRepository;
import com.app.vendia.repository.ClientRepository;
import com.app.vendia.repository.CommandeRepository;
import com.app.vendia.repository.PanierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final PanierRepository panierRepository;
    private final ClientRepository clientRepository;
    private final AdresseRepository adresseRepository;

    @Transactional
    public CommandeResponse creer(Long clientId, CommandeRequest requete) {
        Panier panier = panierRepository.findByClientId(clientId)
                .filter(p -> !p.getLignes().isEmpty())
                .orElseThrow(PanierVideException::new);

        
        for (LignePanier lp : panier.getLignes()) {
            Produit p = lp.getProduit();
            if (!p.isActif()) {
                throw new RessourceIntrouvableException("Produit", p.getId());
            }
            if (p.getStock() < lp.getQuantite()) {
                throw new StockInsuffisantException(p.getNom(), p.getStock(), lp.getQuantite());
            }
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RessourceIntrouvableException("Client", clientId));
        Adresse adresse = resoudreAdresse(client, requete);

        
        Commande commande = commandeRepository.save(
                Commande.builder().client(client).adresse(adresse).build());

        for (LignePanier lp : panier.getLignes()) {
            Produit p = lp.getProduit();
            commande.getLignes().add(LigneCommande.builder()
                    .id(new LigneCommandeId(p.getId(), commande.getId()))
                    .commande(commande)
                    .produit(p)
                    .quantite(lp.getQuantite())
                    .prix(p.getPrix()) 
                    .build());
        }

        
        return versDto(commande);
    }

    @Transactional(readOnly = true)
    public Page<CommandeResumeResponse> historique(Long clientId, Pageable pageable) {
        return commandeRepository.findByClientIdOrderByDateCreationDesc(clientId, pageable)
                .map(this::versResume);
    }

    @Transactional(readOnly = true)
    public CommandeResponse detail(Long clientId, Long commandeId) {
        return versDto(trouverCommande(clientId, commandeId));
    }

    @Transactional
    public CommandeResponse annuler(Long clientId, Long commandeId) {
        Commande commande = trouverCommande(clientId, commandeId);

        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new CommandeNonAnnulableException(commande.getStatut());
        }
        commande.setStatut(StatutCommande.ANNULEE);
        return versDto(commande);
    }

  

    private Commande trouverCommande(Long clientId, Long commandeId) {
      
        return commandeRepository.findByIdAndClientId(commandeId, clientId)
                .orElseThrow(() -> new RessourceIntrouvableException("Commande", commandeId));
    }

    private Adresse resoudreAdresse(Client client, CommandeRequest requete) {
        if (requete.adresseId() != null) {
            return adresseRepository.findById(requete.adresseId())
                    .filter(a -> a.getClient() != null && a.getClient().getId().equals(client.getId()))
                    .orElseThrow(() -> new RessourceIntrouvableException("Adresse", requete.adresseId()));
        }

        AdresseRequest a = requete.adresse();
        return adresseRepository.save(Adresse.builder()
                .client(client)
                .intitule(a.intitule())
                .ligne1(a.ligne1())
                .ligne2(a.ligne2())
                .codePostal(a.codePostal())
                .ville(a.ville())
                .pays(a.pays())
                .build());
    }

    private CommandeResponse versDto(Commande c) {
        var lignes = c.getLignes().stream()
                .map(l -> {
                    Produit p = l.getProduit();
                    BigDecimal sousTotal = l.getPrix().multiply(BigDecimal.valueOf(l.getQuantite()));
                    return new LigneCommandeResponse(
                            p.getId(), p.getNom(), p.getImage(), l.getPrix(), l.getQuantite(), sousTotal);
                })
                .toList();

        BigDecimal total = lignes.stream()
                .map(LigneCommandeResponse::sousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Adresse a = c.getAdresse();
        AdresseResponse adresse = new AdresseResponse(
                a.getId(), a.getIntitule(), a.getLigne1(), a.getLigne2(),
                a.getCodePostal(), a.getVille(), a.getPays());

        return new CommandeResponse(c.getId(), c.getDateCreation(), c.getStatut(), adresse, lignes, total);
    }

    private CommandeResumeResponse versResume(Commande c) {
        int nombreArticles = c.getLignes().stream().mapToInt(LigneCommande::getQuantite).sum();
        BigDecimal total = c.getLignes().stream()
                .map(l -> l.getPrix().multiply(BigDecimal.valueOf(l.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CommandeResumeResponse(c.getId(), c.getDateCreation(), c.getStatut(), nombreArticles, total);
    }
}