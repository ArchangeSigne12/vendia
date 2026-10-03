package com.app.vendia.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogueIntegrationTest extends IntegrationTestBase {

    @Test
    void cycleAdmin_creerCategorieEtProduit_puisLesConsulterPublic() {
        String emailAdmin = "admin-cat" + System.nanoTime() + "@test.com";
        inscrireEtConnecter(emailAdmin, "Azerty123");
        promouvoirAdmin(emailAdmin);
        String tokenAdmin = connecter(emailAdmin, "Azerty123"); // re-login : token avec ROLE_ADMIN

        ResponseEntity<String> categorie = restTemplate.exchange(
                url("/api/v1/categories/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("nom", "Électronique"), avecToken(tokenAdmin)), String.class);
        assertThat(categorie.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long categorieId = json(categorie.getBody()).get("id").asLong();

        ResponseEntity<String> produit = restTemplate.exchange(
                url("/api/v1/produits/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "categorieId", categorieId, "nom", "Clavier mécanique",
                        "prix", 59.90, "stock", 5), avecToken(tokenAdmin)),
                String.class);
        assertThat(produit.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode corpsProduit = json(produit.getBody());
        assertThat(corpsProduit.get("disponible").asBoolean()).isTrue();

        // Consultation publique, SANS token
        ResponseEntity<String> detail = restTemplate.getForEntity(
                url("/api/v1/produits/" + corpsProduit.get("id").asLong()), String.class);
        assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(detail.getBody()).get("nom").asText()).isEqualTo("Clavier mécanique");
    }

    @Test
    void creationCategorie_doitEchouerSansToken() {
        ResponseEntity<String> reponse = restTemplate.exchange(
                url("/api/v1/categories/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("nom", "Interdit")), String.class);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void desactivationProduit_doitLeRendreAbsentDuCatalogueActif() {
        String emailAdmin = "admin-desact" + System.nanoTime() + "@test.com";
        inscrireEtConnecter(emailAdmin, "Azerty123");
        promouvoirAdmin(emailAdmin);
        String tokenAdmin = connecter(emailAdmin, "Azerty123");

        long categorieId = json(restTemplate.exchange(
                url("/api/v1/categories/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("nom", "À supprimer"), avecToken(tokenAdmin)), String.class)
                .getBody()).get("id").asLong();

        long produitId = json(restTemplate.exchange(
                url("/api/v1/produits/admin"), HttpMethod.POST,
                new HttpEntity<>(Map.of("categorieId", categorieId, "nom", "Produit jetable",
                        "prix", 10, "stock", 1), avecToken(tokenAdmin)), String.class)
                .getBody()).get("id").asLong();

        ResponseEntity<Void> suppression = restTemplate.exchange(
                url("/api/v1/produits/admin/" + produitId), HttpMethod.DELETE,
                new HttpEntity<>(avecToken(tokenAdmin)), Void.class);
        assertThat(suppression.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Le produit reste consultable par id (c'est une désactivation logique, pas une suppression physique)
        ResponseEntity<String> detail = restTemplate.getForEntity(url("/api/v1/produits/" + produitId), String.class);
        assertThat(json(detail.getBody()).get("actif").asBoolean()).isFalse();
    }
}
