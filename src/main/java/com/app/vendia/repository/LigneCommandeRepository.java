package com.app.vendia.repository;

import com.app.vendia.model.LigneCommande;
import com.app.vendia.model.LigneCommandeId;
import com.app.vendia.model.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande, LigneCommandeId> {

    // Chiffre d'affaires : somme des lignes des commandes dans les statuts donnés.
    // Les prix sont déjà figés dans LigneCommande, donc fiable même si un produit change de prix depuis.
    @Query("""
           select coalesce(sum(lc.prix * lc.quantite), 0)
           from LigneCommande lc
           where lc.commande.statut in :statuts
           """)
    BigDecimal sommeTotaleParStatutsCommande(@Param("statuts") Collection<StatutCommande> statuts);
}
