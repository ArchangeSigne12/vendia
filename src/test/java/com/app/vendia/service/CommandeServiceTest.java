package com.app.vendia.service;

import com.app.vendia.dto.request.AdresseRequest;
import com.app.vendia.dto.request.CommandeRequest;
import com.app.vendia.dto.response.CommandeResponse;
import com.app.vendia.exception.CommandeNonAnnulableException;
import com.app.vendia.exception.PanierVideException;
import com.app.vendia.model.*;
import com.app.vendia.repository.AdresseRepository;
import com.app.vendia.repository.ClientRepository;
import com.app.vendia.repository.CommandeRepository;
import com.app.vendia.repository.PanierRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandeServiceTest {

    @Mock private CommandeRepository commandeRepository;
    @Mock private PanierRepository panierRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private AdresseRepository adresseRepository;

    @InjectMocks private CommandeService commandeService;

    @Test
    void creer_doitFigerLesPrixCreerLaCommandeEnAttenteEtLaisserLeStockIntact() {
        Client client = Client.builder().id(1L).build();
        Produit produit = Produit.builder().id(10L).nom("Clavier")
                .prix(new BigDecimal("59.90")).stock(5).actif(true).build();
        Panier panier = Panier.builder().id(100L).client(client).build();
        panier.getLignes().add(LignePanier.builder()
                .id(new PanierLigneId(100L, 10L)).panier(panier).produit(produit).quantite(2).build());

        AdresseRequest adresse = new AdresseRequest("Maison", "Rue 1", null, "00237", "Douala", "Cameroun");

        when(panierRepository.findByClientId(1L)).thenReturn(Optional.of(panier));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(adresseRepository.save(any(Adresse.class))).thenAnswer(inv -> {
            Adresse a = inv.getArgument(0);
            a.setId(7L);
            return a;
        });
        when(commandeRepository.save(any(Commande.class))).thenAnswer(inv -> {
            Commande c = inv.getArgument(0);
            c.setId(42L);
            return c;
        });

        CommandeResponse resultat = commandeService.creer(1L, new CommandeRequest(null, adresse));

        assertThat(resultat.id()).isEqualTo(42L);
        assertThat(resultat.statut()).isEqualTo(StatutCommande.EN_ATTENTE);
        assertThat(resultat.lignes()).hasSize(1);
        assertThat(resultat.lignes().get(0).prixUnitaire()).isEqualByComparingTo("59.90");
        assertThat(resultat.total()).isEqualByComparingTo("119.80");
        assertThat(produit.getStock()).isEqualTo(5); // le stock n'est pas décrémenté à la commande
    }

    @Test
    void creer_doitEchouerQuandLePanierEstVide() {
        when(panierRepository.findByClientId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeService.creer(1L, new CommandeRequest(5L, null)))
                .isInstanceOf(PanierVideException.class);
    }

    @Test
    void annuler_doitEchouerQuandLaCommandeEstDejaPayee() {
        Commande commande = Commande.builder().id(5L).statut(StatutCommande.PAYEE).build();
        when(commandeRepository.findByIdAndClientId(5L, 1L)).thenReturn(Optional.of(commande));

        assertThatThrownBy(() -> commandeService.annuler(1L, 5L))
                .isInstanceOf(CommandeNonAnnulableException.class);
    }
}
