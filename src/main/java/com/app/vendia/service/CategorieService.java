package com.app.vendia.service;

import com.app.vendia.model.Categorie;
import com.app.vendia.dto.request.CategorieRequest;
import com.app.vendia.dto.response.CategorieResponse;
import com.app.vendia.exception.RessourceIntrouvableException;
import com.app.vendia.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public List<CategorieResponse> lister() {
        return categorieRepository.findAll().stream().map(this::versDto).toList();
    }

    @Transactional
    public CategorieResponse creer(CategorieRequest requete) {
        Categorie categorie = Categorie.builder()
                .nom(requete.nom())
                .description(requete.description())
                .build();
        return versDto(categorieRepository.save(categorie));
    }

    @Transactional
    public CategorieResponse modifier(Long id, CategorieRequest requete) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Catégorie", id));
        categorie.setNom(requete.nom());
        categorie.setDescription(requete.description());
        return versDto(categorie);
    }

    @Transactional
    public void supprimer(Long id) {
        if (!categorieRepository.existsById(id)) {
            throw new RessourceIntrouvableException("Catégorie", id);
        }
        categorieRepository.deleteById(id);
    }

    private CategorieResponse versDto(Categorie c) {
        return new CategorieResponse(c.getId(), c.getNom(), c.getDescription());
    }
}
