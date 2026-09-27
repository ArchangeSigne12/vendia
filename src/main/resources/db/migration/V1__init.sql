-- V1__init.sql
-- Schéma initial de Vendia, dérivé du modèle logique de données du cahier des charges.

CREATE TABLE categorie (
    id_categorie BIGSERIAL PRIMARY KEY,
    nom          VARCHAR(150) NOT NULL,
    description  TEXT
);

CREATE TABLE client (
    id_client     BIGSERIAL PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    telephone     VARCHAR(30),
    mot_de_passe  VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'ROLE_CLIENT'
);

CREATE TABLE adresse (
    id_adresse   BIGSERIAL PRIMARY KEY,
    id_client    BIGINT REFERENCES client(id_client),
    intitule     VARCHAR(100),
    ligne_1      VARCHAR(255) NOT NULL,
    ligne_2      VARCHAR(255),
    code_postal  VARCHAR(20)  NOT NULL,
    ville        VARCHAR(100) NOT NULL,
    pays         VARCHAR(100) NOT NULL
);

CREATE TABLE produit (
    id_produit    BIGSERIAL PRIMARY KEY,
    id_categorie  BIGINT NOT NULL REFERENCES categorie(id_categorie),
    nom           VARCHAR(200) NOT NULL,
    prix          NUMERIC(10,2) NOT NULL CHECK (prix >= 0),
    description   TEXT,
    stock         INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    actif         BOOLEAN NOT NULL DEFAULT TRUE,
    image         VARCHAR(500),
    version       BIGINT NOT NULL DEFAULT 0   -- verrouillage optimiste (@Version JPA)
);

CREATE TABLE note (
    id_note       BIGSERIAL PRIMARY KEY,
    id_produit    BIGINT NOT NULL REFERENCES produit(id_produit),
    id_client     BIGINT NOT NULL REFERENCES client(id_client),
    note          SMALLINT NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire   TEXT
);

CREATE TABLE commande (
    id_commande     BIGSERIAL PRIMARY KEY,
    id_client       BIGINT NOT NULL REFERENCES client(id_client),
    id_adresse      BIGINT NOT NULL REFERENCES adresse(id_adresse),
    date_creation   TIMESTAMP NOT NULL DEFAULT now(),
    statut          VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE'
);

CREATE TABLE ligne_commande (
    id_produit    BIGINT NOT NULL REFERENCES produit(id_produit),
    id_commande   BIGINT NOT NULL REFERENCES commande(id_commande),
    quantite      INTEGER NOT NULL CHECK (quantite > 0),
    prix          NUMERIC(10,2) NOT NULL,  -- snapshot du prix unitaire au moment de la commande
    date          TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (id_produit, id_commande)
);

CREATE TABLE paiement (
    id_paiement             BIGSERIAL PRIMARY KEY,
    id_commande             BIGINT NOT NULL REFERENCES commande(id_commande),
    date_paiement           TIMESTAMP,
    reference_transaction   VARCHAR(255),
    montant                 NUMERIC(10,2) NOT NULL,
    methode                 VARCHAR(50),
    statut                  VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE'
);

-- Index de performance (section 6.3 du cahier des charges)
CREATE INDEX idx_produit_categorie ON produit(id_categorie);
CREATE INDEX idx_produit_nom       ON produit(nom);
CREATE INDEX idx_client_email      ON client(email);
CREATE INDEX idx_commande_client   ON commande(id_client);
