

CREATE TABLE panier (
    id_panier      BIGSERIAL PRIMARY KEY,
    id_client      BIGINT NOT NULL UNIQUE REFERENCES client(id_client),
    date_creation  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ligne_panier (
    id_panier   BIGINT NOT NULL REFERENCES panier(id_panier) ON DELETE CASCADE,
    id_produit  BIGINT NOT NULL REFERENCES produit(id_produit),
    quantite    INTEGER NOT NULL CHECK (quantite > 0),
    PRIMARY KEY (id_panier, id_produit)
);
