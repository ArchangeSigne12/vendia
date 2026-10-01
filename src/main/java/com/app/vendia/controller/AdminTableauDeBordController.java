package com.app.vendia.controller;

import com.app.vendia.dto.response.TableauDeBordResponse;
import com.app.vendia.service.TableauDeBordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/tableau-de-bord")
@RequiredArgsConstructor
@Tag(name = "Admin - Tableau de bord")
public class AdminTableauDeBordController {

    private final TableauDeBordService tableauDeBordService;

    @GetMapping
    public TableauDeBordResponse obtenir() {
        return tableauDeBordService.obtenir();
    }
}
