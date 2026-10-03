package com.app.vendia.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PanierCommandeIntegrationTest extends IntegrationTestBase {

    private long creerCategorie(String tokenAdmin, String nom) {
        ResponseEntity<String> reponse = restTemplate.exchange(
                url("/api/v1/categories/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("nom", nom), avecToken(tokenAdmin)), String.class);
        return json(reponse.getBody()).get("id").asLong();
    }

    private long creerProduit(String tokenAdmin, long categorieId, String nom, double prix, int stock) {
        ResponseEntity<String> reponse = restTemplate.exchange(
                url("/api/v1/produits/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("categorieId", categorieId, "nom", nom, "prix", prix, "stock", stock),
                        avecToken(tokenAdmin)),
                String.class);
        return json(reponse.getBody()).get("id").asLong();
    }

    private ResponseEntity<String> ajouterAuPanier(String tokenClient, long produitId, int quantite) {
        return restTemplate.exchange(
                url("/api/v1/panier/lignes"), HttpMethod.POST,
                new HttpEntity<>(Map.of("produitId", produitId, "quantite", quantite), avecToken(tokenClient)),
                String.class);
    }

    @Test
    void ajoutsRepetes_doiventCumulerLaQuantiteEtRefuserSiStockInsuffisant() {
        String emailAdmin = "admin-panier" + System.nanoTime() + "@test.com";
        inscrireEtConnecter(emailAdmin, "Azerty123");
        promouvoirAdmin(emailAdmin);
        String tokenAdmin = connecter(emailAdmin, "Azerty123");

        long categorieId = creerCategorie(tokenAdmin, "Catégorie panier");
        long produitId = creerProduit(tokenAdmin, categorieId, "Produit panier", 1000, 3);

        String tokenClient = inscrireEtConnecter("client-panier" + System.nanoTime() + "@test.com", "Azerty123");

        ajouterAuPanier(tokenClient, produitId, 1);
        ResponseEntity<String> apresDeuxiemeAjout = ajouterAuPanier(tokenClient, produitId, 1);

        assertThat(apresDeuxiemeAjout.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode ligne = json(apresDeuxiemeAjout.getBody()).get("lignes").get(0);
        assertThat(ligne.get("quantite").asInt()).isEqualTo(2); // 1 + 1 cumulés, pas remplacés

        ResponseEntity<String> refus = restTemplate.exchange(
                url("/api/v1/panier/lignes"), HttpMethod.POST,
                new HttpEntity<>(Map.of("produitId", produitId, "quantite", 10), avecToken(tokenClient)),
                String.class);
        assertThat(refus.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(champ(refus.getBody(), "error")).isEqualTo("STOCK_INSUFFISANT");
    }

    @Test
    void creationCommande_doitFigerLesPrixIndependammentDesChangementsFuturs() {
        String emailAdmin = "admin-cmd" + System.nanoTime() + "@test.com";
        inscrireEtConnecter(emailAdmin, "Azerty123");
        promouvoirAdmin(emailAdmin);
        String tokenAdmin = connecter(emailAdmin, "Azerty123");

        long categorieId = creerCategorie(tokenAdmin, "Catégorie commande");
        long produitId = creerProduit(tokenAdmin, categorieId, "Produit commande", 1000, 5);

        String tokenClient = inscrireEtConnecter("client-cmd" + System.nanoTime() + "@test.com", "Azerty123");
        ajouterAuPanier(tokenClient, produitId, 2);

        Map<String, Object> adresse = Map.of(
                "ligne1", "Rue 1", "codePostal", "00237", "ville", "Douala", "pays", "Cameroun");
        ResponseEntity<String> commande = restTemplate.exchange(
                url("/api/v1/commandes"), HttpMethod.POST,
                new HttpEntity<>(Map.of("adresse", adresse), avecToken(tokenClient)), String.class);

        assertThat(commande.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode corps = json(commande.getBody());
        assertThat(corps.get("statut").asText()).isEqualTo("EN_ATTENTE");
        assertThat(corps.get("total").asDouble()).isEqualTo(2000.0);
        long commandeId = corps.get("id").asLong();

        // Le prix change APRÈS la commande : elle ne doit pas bouger.
        restTemplate.exchange(
                url("/api/v1/produits/admin/" + produitId), HttpMethod.PUT,
                new HttpEntity<>(Map.of("categorieId", categorieId, "nom", "Produit commande",
                        "prix", 5000, "stock", 5), avecToken(tokenAdmin)),
                String.class);

        ResponseEntity<String> detail = restTemplate.exchange(
                url("/api/v1/commandes/" + commandeId), HttpMethod.GET,
                new HttpEntity<>(avecToken(tokenClient)), String.class);
        assertThat(json(detail.getBody()).get("total").asDouble()).isEqualTo(2000.0); // inchangé
    }

    @Test
    void creationCommande_doitEchouerSiLePanierEstVide() {
        String tokenClient = inscrireEtConnecter("client-vide" + System.nanoTime() + "@test.com", "Azerty123");

        Map<String, Object> adresse = Map.of(
                "ligne1", "Rue 1", "codePostal", "00237", "ville", "Douala", "pays", "Cameroun");
        ResponseEntity<String> commande = restTemplate.exchange(
                url("/api/v1/commandes"), HttpMethod.POST,
                new HttpEntity<>(Map.of("adresse", adresse), avecToken(tokenClient)), String.class);

        assertThat(commande.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(champ(commande.getBody(), "error")).isEqualTo("PANIER_VIDE");
    }
}
