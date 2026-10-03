package com.app.vendia.integration;

import com.app.vendia.model.Commande;
import com.app.vendia.model.Produit;
import com.app.vendia.model.StatutCommande;
import com.app.vendia.repository.CommandeRepository;
import com.app.vendia.repository.ProduitRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdminBackOfficeIntegrationTest extends IntegrationTestBase {

    @Autowired private ProduitRepository produitRepository;
    @Autowired private CommandeRepository commandeRepository;

    @Test
    void changerStatut_doitRefuserUneTransitionNonAutorisee() {
        String emailAdmin = "admin-transition" + System.nanoTime() + "@test.com";
        inscrireEtConnecter(emailAdmin, "Azerty123");
        promouvoirAdmin(emailAdmin);
        String tokenAdmin = connecter(emailAdmin, "Azerty123");

        long commandeId = creerCommandeEnAttente(tokenAdmin, "client-transition" + System.nanoTime() + "@test.com");

    
        ResponseEntity<String> reponse = restTemplate.exchange(
                url("/api/v1/admin/commandes/" + commandeId + "/statut"), HttpMethod.PUT,
                new HttpEntity<>(Map.of("statut", "LIVREE"), avecToken(tokenAdmin)), String.class);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(champ(reponse.getBody(), "error")).isEqualTo("TRANSITION_STATUT_INVALIDE");
    }

    @Test
    void changerStatut_nePermetJamaisDeForcerPayee() {
        String emailAdmin = "admin-payee" + System.nanoTime() + "@test.com";
        inscrireEtConnecter(emailAdmin, "Azerty123");
        promouvoirAdmin(emailAdmin);
        String tokenAdmin = connecter(emailAdmin, "Azerty123");

        long commandeId = creerCommandeEnAttente(tokenAdmin, "client-payee" + System.nanoTime() + "@test.com");

        ResponseEntity<String> reponse = restTemplate.exchange(
                url("/api/v1/admin/commandes/" + commandeId + "/statut"), HttpMethod.PUT,
                new HttpEntity<>(Map.of("statut", "PAYEE"), avecToken(tokenAdmin)), String.class);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void annulerUneCommandePayee_doitRestituerLeStock() {
        String emailAdmin = "admin-restitution" + System.nanoTime() + "@test.com";
        inscrireEtConnecter(emailAdmin, "Azerty123");
        promouvoirAdmin(emailAdmin);
        String tokenAdmin = connecter(emailAdmin, "Azerty123");

        long categorieId = json(restTemplate.exchange(
                url("/api/v1/categories/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("nom", "Catégorie restitution"), avecToken(tokenAdmin)), String.class)
                .getBody()).get("id").asLong();

        long produitId = json(restTemplate.exchange(
                url("/api/v1/produits/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("categorieId", categorieId, "nom", "Produit restitution",
                        "prix", 500, "stock", 5), avecToken(tokenAdmin)), String.class)
                .getBody()).get("id").asLong();

        String tokenClient = inscrireEtConnecter("client-restitution" + System.nanoTime() + "@test.com", "Azerty123");
        restTemplate.exchange(
                url("/api/v1/panier/lignes"), HttpMethod.POST,
                new HttpEntity<>(Map.of("produitId", produitId, "quantite", 2), avecToken(tokenClient)), String.class);

        Map<String, Object> adresse = Map.of(
                "ligne1", "Rue 1", "codePostal", "00237", "ville", "Douala", "pays", "Cameroun");
        JsonNode commande = json(restTemplate.exchange(
                url("/api/v1/commandes"), HttpMethod.POST,
                new HttpEntity<>(Map.of("adresse", adresse), avecToken(tokenClient)), String.class).getBody());
        long commandeId = commande.get("id").asLong();

        
        Produit produit = produitRepository.findById(produitId).orElseThrow();
        int stockAvantPaiement = produit.getStock();
        produit.setStock(stockAvantPaiement - 2);
        produitRepository.save(produit);

        Commande commandeEntite = commandeRepository.findById(commandeId).orElseThrow();
        commandeEntite.setStatut(StatutCommande.PAYEE);
        commandeRepository.save(commandeEntite);

        ResponseEntity<String> annulation = restTemplate.exchange(
                url("/api/v1/admin/commandes/" + commandeId + "/statut"), HttpMethod.PUT,
                new HttpEntity<>(Map.of("statut", "ANNULEE"), avecToken(tokenAdmin)), String.class);
        assertThat(annulation.getStatusCode()).isEqualTo(HttpStatus.OK);

        Produit produitApres = produitRepository.findById(produitId).orElseThrow();
        assertThat(produitApres.getStock()).isEqualTo(stockAvantPaiement); // restitué intégralement
    }

    @Test
    void tableauDeBord_doitRefuserUnClientNonAdmin() {
        String tokenClient = inscrireEtConnecter("client-dashboard" + System.nanoTime() + "@test.com", "Azerty123");

        ResponseEntity<String> reponse = restTemplate.exchange(
                url("/api/v1/admin/tableau-de-bord"), HttpMethod.GET,
                new HttpEntity<>(avecToken(tokenClient)), String.class);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private long creerCommandeEnAttente(String tokenAdmin, String emailClient) {
        long categorieId = json(restTemplate.exchange(
                url("/api/v1/categories/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("nom", "Catégorie " + System.nanoTime()), avecToken(tokenAdmin)), String.class)
                .getBody()).get("id").asLong();

        long produitId = json(restTemplate.exchange(
                url("/api/v1/produits/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("categorieId", categorieId, "nom", "Produit " + System.nanoTime(),
                        "prix", 100, "stock", 5), avecToken(tokenAdmin)), String.class)
                .getBody()).get("id").asLong();

        String tokenClient = inscrireEtConnecter(emailClient, "Azerty123");
        restTemplate.exchange(
                url("/api/v1/panier/lignes"), HttpMethod.POST,
                new HttpEntity<>(Map.of("produitId", produitId, "quantite", 1), avecToken(tokenClient)), String.class);

        Map<String, Object> adresse = Map.of(
                "ligne1", "Rue 1", "codePostal", "00237", "ville", "Douala", "pays", "Cameroun");
        return json(restTemplate.exchange(
                url("/api/v1/commandes"), HttpMethod.POST,
                new HttpEntity<>(Map.of("adresse", adresse), avecToken(tokenClient)), String.class).getBody())
                .get("id").asLong();
    }
}
