package co.analisys.miembros.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;

import co.analisys.miembros.model.Miembro;
import co.analisys.miembros.service.MiembroService;

@RestController
@RequestMapping("/api/miembro")
@Tag(name = "Miembros", description = "Gestion de los miembros del gimnasio")
public class MiembroController {

    private final MiembroService miembroService;

    public MiembroController(MiembroService miembroService) {
        this.miembroService = miembroService;
    }

    @Operation(summary = "Registrar un miembro", description = "Crea un nuevo miembro del gimnasio. Roles permitidos: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public Miembro registrarMiembro(@RequestBody Miembro miembro) {
        return miembroService.registrarMiembro(miembro);
    }

    @Operation(summary = "Listar miembros", description = "Retorna todos los miembros registrados. Roles permitidos: ADMIN, TRAINER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping
    public List<Miembro> obtenerTodosLosMiembros() {
        return miembroService.obtenerTodosLosMiembros();
    }

}
