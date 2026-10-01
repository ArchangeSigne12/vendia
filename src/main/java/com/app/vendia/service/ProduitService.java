package com.app.vendia.service;

import com.app.vendia.model.Categorie;
import com.app.vendia.model.Produit;
import com.app.vendia.dto.request.ProduitRequest;
import com.app.vendia.dto.response.ProduitResponse;
import com.app.vendia.exception.RessourceIntrouvableException;
import com.app.vendia.repository.CategorieRepository;
import com.app.vendia.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.vendia.dto.request.AjusterStockRequest;
import com.app.vendia.exception.AjustementStockInvalideException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    public Page<ProduitResponse> lister(Long categorieId, Pageable pageable) {
        Page<Produit> page = (categorieId != null)
                ? produitRepository.findByActifTrueAndCategorieId(categorieId, pageable)
                : produitRepository.findAll(pageable);
        return page.map(this::versDto);
    }

    public Page<ProduitResponse> rechercher(String motCle, Pageable pageable) {
        return produitRepository.rechercher(motCle, pageable).map(this::versDto);
    }

    public ProduitResponse detail(Long id) {
        return versDto(trouver(id));
    }

    @Transactional
    public ProduitResponse creer(ProduitRequest requete) {
        Categorie categorie = categorieRepository.findById(requete.categorieId())
                .orElseThrow(() -> new RessourceIntrouvableException("Catégorie", requete.categorieId()));

        Produit produit = Produit.builder()
                .categorie(categorie)
                .nom(requete.nom())
                .prix(requete.prix())
                .description(requete.description())
                .stock(requete.stock())
                .image(requete.image())
                .actif(true)
                .build();

        return versDto(produitRepository.save(produit));
    }

    @Transactional
    public ProduitResponse modifier(Long id, ProduitRequest requete) {
        Produit produit = trouver(id);
        Categorie categorie = categorieRepository.findById(requete.categorieId())
                .orElseThrow(() -> new RessourceIntrouvableException("Catégorie", requete.categorieId()));

        produit.setCategorie(categorie);
        produit.setNom(requete.nom());
        produit.setPrix(requete.prix());
        produit.setDescription(requete.description());
        produit.setStock(requete.stock());
        produit.setImage(requete.image());

        return versDto(produit); 
    }

    
    @Transactional
    public void desactiver(Long id) {
        Produit produit = trouver(id);
        produit.setActif(false);
    }

    @Transactional
    public ProduitResponse ajusterStock(Long id, AjusterStockRequest requete) {
    Produit produit = trouver(id); 

    int nouveauStock = produit.getStock() + requete.delta();
    if (nouveauStock < 0) {
        throw new AjustementStockInvalideException(produit.getNom(), produit.getStock(), requete.delta());
    }

    log.info("Ajustement stock produit {} : {} -> {} (delta {}, motif : {})",
            produit.getId(), produit.getStock(), nouveauStock, requete.delta(), requete.motif());

    produit.setStock(nouveauStock);
    return versDto(produit);
    }

    private Produit trouver(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Produit", id));
    }

    private ProduitResponse versDto(Produit p) {
        return new ProduitResponse(
                p.getId(),
                p.getCategorie().getId(),
                p.getCategorie().getNom(),
                p.getNom(),
                p.getPrix(),
                p.getDescription(),
                p.getStock(),
                p.isActif(),
                p.getImage(),
                p.getStock() != null && p.getStock() > 0
        );
    }
}
