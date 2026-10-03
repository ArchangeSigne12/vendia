package com.app.vendia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void inscription_doitRenvoyerDesTokens() {
        String email = "auth" + System.nanoTime() + "@test.com";
        Map<String, Object> corps = Map.of(
                "nom", "Test", "prenom", "User", "email", email, "motDePasse", "Azerty123");

        ResponseEntity<String> reponse = restTemplate.postForEntity(url("/api/v1/auth/register"), corps, String.class);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(champ(reponse.getBody(), "accessToken")).isNotBlank();
        assertThat(champ(reponse.getBody(), "refreshToken")).isNotBlank();
    }

    @Test
    void inscription_doitEchouerSiEmailDejaUtilise() {
        String email = "doublon" + System.nanoTime() + "@test.com";
        Map<String, Object> corps = Map.of(
                "nom", "Test", "prenom", "User", "email", email, "motDePasse", "Azerty123");

        restTemplate.postForEntity(url("/api/v1/auth/register"), corps, String.class);
        ResponseEntity<String> deuxieme = restTemplate.postForEntity(url("/api/v1/auth/register"), corps, String.class);

        assertThat(deuxieme.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(champ(deuxieme.getBody(), "error")).isEqualTo("EMAIL_DEJA_UTILISE");
    }

    @Test
    void routeProtegee_doitRefuserSansToken() {
        ResponseEntity<String> reponse = restTemplate.getForEntity(url("/api/v1/panier"), String.class);
        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void routeAdmin_doitRefuserUnClientNonAdmin() {
        String email = "client-ordinaire" + System.nanoTime() + "@test.com";
        String token = inscrireEtConnecter(email, "Azerty123");

        ResponseEntity<String> reponse = restTemplate.exchange(
                url("/api/v1/categories/admin"), org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(Map.of("nom", "Interdit"), avecToken(token)), String.class);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    
    @Test
    void logoutPuisRefresh_doitEchouerApresLogout() {
        String email = "logout" + System.nanoTime() + "@test.com";
        restTemplate.postForEntity(url("/api/v1/auth/register"), Map.of(
                "nom", "Test", "prenom", "User", "email", email, "motDePasse", "Azerty123"), String.class);

        ResponseEntity<String> login = restTemplate.postForEntity(
                url("/api/v1/auth/login"), Map.of("email", email, "motDePasse", "Azerty123"), String.class);
        String refreshToken = champ(login.getBody(), "refreshToken");

        // Le refresh fonctionne tant qu'on ne s'est pas déconnecté
        ResponseEntity<String> refreshAvant = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"), Map.of("refreshToken", refreshToken), String.class);
        assertThat(refreshAvant.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> logout = restTemplate.postForEntity(
                url("/api/v1/auth/logout"), Map.of("refreshToken", refreshToken), Void.class);
        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> refreshApres = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"), Map.of("refreshToken", refreshToken), String.class);
        assertThat(refreshApres.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
