package com.app.vendia.repository;

import com.app.vendia.model.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    Page<Produit> findByActifTrueAndCategorieId(Long categorieId, Pageable pageable);

    @Query("""
           select p from Produit p
           where p.actif = true
             and (lower(p.nom) like lower(concat('%', :q, '%'))
                  or lower(p.description) like lower(concat('%', :q, '%')))
           """)
    Page<Produit> rechercher(@Param("q") String motCle, Pageable pageable);

    long countByStockAndActifTrue(int i);
}
