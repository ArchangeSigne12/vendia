package com.app.vendia.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
public class LigneCommandeId implements Serializable {

    private Long idProduit;
    private Long idCommande;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LigneCommandeId that)) return false;
        return Objects.equals(idProduit, that.idProduit) && Objects.equals(idCommande, that.idCommande);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProduit, idCommande);
    }
}
