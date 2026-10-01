package com.app.vendia.service;

import com.app.vendia.dto.response.TableauDeBordResponse;
import com.app.vendia.model.StatutCommande;
import com.app.vendia.repository.CommandeRepository;
import com.app.vendia.repository.LigneCommandeRepository;
import com.app.vendia.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TableauDeBordService {

    private static final Set<StatutCommande> STATUTS_COMPTABILISES =
            EnumSet.of(StatutCommande.PAYEE, StatutCommande.EN_PREPARATION,
                    StatutCommande.EXPEDIEE, StatutCommande.LIVREE);

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final ProduitRepository produitRepository;

    @Transactional(readOnly = true)
    public TableauDeBordResponse obtenir() {
        return new TableauDeBordResponse(
                commandeRepository.count(),
                commandeRepository.countByStatut(StatutCommande.EN_ATTENTE),
                commandeRepository.countByStatut(StatutCommande.EN_PREPARATION),
                ligneCommandeRepository.sommeTotaleParStatutsCommande(STATUTS_COMPTABILISES),
                produitRepository.countByStockAndActifTrue(0)
        );
    }
}
