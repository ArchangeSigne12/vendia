package com.app.vendia.service;

import com.app.vendia.model.Client;
import com.app.vendia.model.Panier;
import com.app.vendia.model.Produit;
import com.app.vendia.dto.response.PanierResponse;
import com.app.vendia.exception.StockInsuffisantException;
import com.app.vendia.repository.ClientRepository;
import com.app.vendia.repository.PanierRepository;
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
class PanierServiceTest {

    @Mock private PanierRepository panierRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private ProduitRepository produitRepository;

    @InjectMocks private PanierService panierService;

    @Test
    void ajouterLigne_doitCreerLePanierEtAjouterLaLigneQuandToutEstDisponible() {
        Client client = Client.builder().id(1L).build();
        Produit produit = Produit.builder().id(10L).nom("Clavier mécanique")
                .prix(new BigDecimal("59.90")).stock(5).build();
        Panier panier = Panier.builder().id(100L).client(client).build();

        when(panierRepository.findByClientId(1L)).thenReturn(Optional.empty());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);
        when(produitRepository.findById(10L)).thenReturn(Optional.of(produit));

        PanierResponse resultat = panierService.ajouterLigne(1L, 10L, 2);

        assertThat(resultat.lignes()).hasSize(1);
        assertThat(resultat.lignes().get(0).quantite()).isEqualTo(2);
        assertThat(resultat.total()).isEqualByComparingTo("119.80");
    }

    @Test
    void ajouterLigne_doitEchouerQuandLeStockEstInsuffisant() {
        Client client = Client.builder().id(1L).build();
        Produit produit = Produit.builder().id(10L).nom("Clavier mécanique")
                .prix(new BigDecimal("59.90")).stock(1).build();
        Panier panier = Panier.builder().id(100L).client(client).build();

        when(panierRepository.findByClientId(1L)).thenReturn(Optional.of(panier));
        when(produitRepository.findById(10L)).thenReturn(Optional.of(produit));

        assertThatThrownBy(() -> panierService.ajouterLigne(1L, 10L, 3))
                .isInstanceOf(StockInsuffisantException.class);
    }
}
