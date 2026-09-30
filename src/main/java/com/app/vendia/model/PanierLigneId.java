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
public class PanierLigneId implements Serializable {

    private Long idPanier;
    private Long idProduit;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PanierLigneId that)) return false;
        return Objects.equals(idPanier, that.idPanier) && Objects.equals(idProduit, that.idProduit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPanier, idProduit);
    }
}
