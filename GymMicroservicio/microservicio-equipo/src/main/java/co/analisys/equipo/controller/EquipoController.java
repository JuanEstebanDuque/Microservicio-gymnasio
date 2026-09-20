package co.analisys.equipo.controller;

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

import co.analisys.equipo.model.Equipo;
import co.analisys.equipo.service.EquipoService;

@RestController
@RequestMapping("/api/equipo")
@Tag(name = "Equipo", description = "Inventario de equipos del gimnasio")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @Operation(summary = "Registrar un equipo", description = "Agrega un equipo al inventario. Roles permitidos: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public Equipo registrarEquipo(@RequestBody Equipo equipo) {
        return equipoService.agregarEquipo(equipo);
    }

    @Operation(summary = "Listar equipos", description = "Retorna todos los equipos del inventario. Roles permitidos: ADMIN, TRAINER, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public List<Equipo> obtenerTodosLosEquipos() {
        return equipoService.obtenerTodosLosEquipos();
    }
}
