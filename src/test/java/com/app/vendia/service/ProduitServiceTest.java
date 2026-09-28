package com.app.vendia.service;

import com.app.vendia.model.Categorie;
import com.app.vendia.model.Produit;
import com.app.vendia.dto.request.ProduitRequest;
import com.app.vendia.dto.response.ProduitResponse;
import com.app.vendia.exception.RessourceIntrouvableException;
import com.app.vendia.repository.CategorieRepository;
import com.app.vendia.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {

    @Mock private ProduitRepository produitRepository;
    @Mock private CategorieRepository categorieRepository;

    @InjectMocks private ProduitService produitService;

    @Test
    void creer_doitEnregistrerLeProduitQuandLaCategorieExiste() {
        Categorie categorie = Categorie.builder().id(1L).nom("Électronique").build();
        ProduitRequest requete = new ProduitRequest(1L, "Casque audio", new BigDecimal("49.99"), "Casque sans fil", 10, null);

        when(categorieRepository.findById(1L)).thenReturn(Optional.of(categorie));
        when(produitRepository.save(any(Produit.class))).thenAnswer(inv -> {
            Produit p = inv.getArgument(0);
            p.setId(42L);
            return p;
        });

        ProduitResponse resultat = produitService.creer(requete);

        assertThat(resultat.id()).isEqualTo(42L);
        assertThat(resultat.nom()).isEqualTo("Casque audio");
        assertThat(resultat.disponible()).isTrue();
        verify(produitRepository).save(any(Produit.class));
    }

    @Test
    void creer_doitEchouerQuandLaCategorieEstIntrouvable() {
        ProduitRequest requete = new ProduitRequest(99L, "Produit fantôme", BigDecimal.TEN, null, 5, null);
        when(categorieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produitService.creer(requete))
                .isInstanceOf(RessourceIntrouvableException.class);

        verify(produitRepository, never()).save(any());
    }
}
