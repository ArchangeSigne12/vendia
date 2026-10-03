package com.app.vendia.service;

import com.app.vendia.exception.TransitionStatutInvalideException;
import com.app.vendia.model.*;
import com.app.vendia.repository.CommandeRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCommandeServiceTest {

    @Mock private CommandeRepository commandeRepository;
    @Mock private ProduitRepository produitRepository;

    @InjectMocks private AdminCommandeService adminCommandeService;

    @Test
    void changerStatut_doitRestituerLeStockQuandUneCommandePayeeEstAnnulee() {
        Produit produit = Produit.builder().id(10L).nom("Clavier").stock(3).build();
        Client client = Client.builder().id(1L).build();
        Commande commande = Commande.builder().id(1L).statut(StatutCommande.PAYEE).client(client).build();
        commande.getLignes().add(LigneCommande.builder()
                .id(new LigneCommandeId(10L, 1L)).commande(commande).produit(produit)
                .quantite(2).prix(new BigDecimal("59.90")).build());

        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(produitRepository.findById(10L)).thenReturn(Optional.of(produit));

        adminCommandeService.changerStatut(1L, StatutCommande.ANNULEE);

        assertThat(produit.getStock()).isEqualTo(5); 
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.ANNULEE);
    }

    @Test
    void changerStatut_doitRefuserUneTransitionNonAutorisee() {
        Commande commande = Commande.builder().id(1L).statut(StatutCommande.LIVREE).build();
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));

        assertThatThrownBy(() -> adminCommandeService.changerStatut(1L, StatutCommande.EN_PREPARATION))
                .isInstanceOf(TransitionStatutInvalideException.class);
    }

    @Test
    void changerStatut_nePermetJamaisDeForcerPayeeDepuisEnAttente() {
        Commande commande = Commande.builder().id(1L).statut(StatutCommande.EN_ATTENTE).build();
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));

        assertThatThrownBy(() -> adminCommandeService.changerStatut(1L, StatutCommande.PAYEE))
                .isInstanceOf(TransitionStatutInvalideException.class);
    }
}
