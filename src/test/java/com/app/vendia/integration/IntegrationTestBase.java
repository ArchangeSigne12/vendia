package com.app.vendia.integration;

import com.app.vendia.model.Role;
import com.app.vendia.repository.ClientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * Base commune des tests d'intégration : un vrai PostgreSQL (Testcontainers),
 * Flyway applique les vraies migrations dessus, et les requêtes passent par
 * de vrais appels HTTP (TestRestTemplate) — donc par le vrai filtre JWT, la
 * vraie sécurité, pas de mock.
 *
 * IMPORTANT : le conteneur est démarré ICI, dans un bloc statique, et
 * JAMAIS arrêté explicitement — c'est voulu. Avec @Container + @Testcontainers,
 * le conteneur est arrêté à la fin de CHAQUE classe de test, alors que Spring
 * met en cache le contexte applicatif (et donc l'URL de connexion) entre les
 * classes : la classe suivante démarrerait un nouveau conteneur sur un port
 * différent, mais le contexte caché garderait l'ancienne URL, maintenant
 * morte. D'où des "Connection refused" en cascade à partir de la 2e classe.
 * Démarrer manuellement et ne jamais arrêter (le conteneur compagnon "Ryuk"
 * de Testcontainers le supprime tout seul à la fin du process `mvn test`)
 * est le pattern "conteneur singleton" officiellement recommandé par
 * Testcontainers pour ce cas précis.
 *
 * Volontairement absent : le paiement Fapshi. FapshiClient appelle un vrai
 * serveur HTTP externe (pas injecté comme une interface qu'on peut
 * substituer facilement) — le tester en intégration demanderait un faux
 * serveur HTTP (type WireMock), qui est une dépendance Maven en plus, donc
 * un risque de version à vérifier. Non fait ici par souci de budget ; la
 * logique de PaiementService est déjà couverte par des tests unitaires
 * (FapshiClient mocké) dans PaiementServiceTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void proprietesBaseDeDonnees(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected ClientRepository clientRepository;

    /**
     * Construit volontairement SON PROPRE TestRestTemplate (pas @Autowired), pour utiliser
     * un client HTTP basé sur java.net.http.HttpClient (JdkClientHttpRequestFactory) plutôt
     * que le client historique java.net.HttpURLConnection utilisé par défaut. Ce dernier a un
     * bug ancien et documenté : il tente de "ré-authentifier" automatiquement en interne dès
     * qu'il reçoit un 401, et plante dans ce processus — peu importe le mode streaming (une
     * première tentative de correction par setOutputStreaming(false) n'a d'ailleurs pas
     * suffi, précisément parce que ce n'était pas la vraie cause). HttpClient ne reproduit
     * pas ce comportement. TestRestTemplate garde son comportement habituel (ne lève pas
     * d'exception sur les 4xx/5xx) peu importe comment on le construit.
     */
    protected final TestRestTemplate restTemplate = construireRestTemplate();

    private static TestRestTemplate construireRestTemplate() {
        TestRestTemplate template = new TestRestTemplate();
        // Appel direct (pas via RestTemplateBuilder.requestFactory(...), ambigu entre ses
        // surcharges selon la version de Spring Boot) : aucune ambiguïté possible ici.
        template.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        return template;
    }

    protected final ObjectMapper mapper = new ObjectMapper();

    protected String url(String chemin) {
        return "http://localhost:" + port + chemin;
    }

    /** Inscrit un client (email unique à fournir par l'appelant) et renvoie son accessToken. */
    protected String inscrireEtConnecter(String email, String motDePasse) {
        Map<String, Object> inscription = Map.of(
                "nom", "Test", "prenom", "Utilisateur", "email", email, "motDePasse", motDePasse);
        restTemplate.postForEntity(url("/api/v1/auth/register"), inscription, String.class);
        return connecter(email, motDePasse);
    }

    protected String connecter(String email, String motDePasse) {
        Map<String, Object> corps = Map.of("email", email, "motDePasse", motDePasse);
        ResponseEntity<String> reponse = restTemplate.postForEntity(url("/api/v1/auth/login"), corps, String.class);
        return champ(reponse.getBody(), "accessToken");
    }

    /** Passe un client existant en ROLE_ADMIN directement en base (comme le fait l'utilisateur à la main en local). */
    protected void promouvoirAdmin(String email) {
        var client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Client introuvable : " + email));
        client.setRole(Role.ROLE_ADMIN);
        clientRepository.save(client);
    }

    protected HttpHeaders avecToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected String champ(String corpsJson, String nomChamp) {
        return json(corpsJson).get(nomChamp).asText();
    }

    protected JsonNode json(String corpsJson) {
        try {
            return mapper.readTree(corpsJson);
        } catch (Exception e) {
            throw new RuntimeException("JSON invalide reçu : " + corpsJson, e);
        }
    }
}