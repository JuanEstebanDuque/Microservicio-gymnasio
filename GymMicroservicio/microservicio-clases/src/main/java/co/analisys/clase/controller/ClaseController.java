package co.analisys.clase.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

import co.analisys.clase.dto.CambioHorarioRequest;
import co.analisys.clase.model.Clase;
import co.analisys.clase.service.ClaseService;

@RestController
@RequestMapping("/api/clase")
@Tag(name = "Clases", description = "Programacion y consulta de clases del gimnasio")
public class ClaseController {

    private final ClaseService claseService;

    public ClaseController(ClaseService claseService) {
        this.claseService = claseService;
    }

    @Operation(summary = "Programar una clase", description = "Crea una clase validando por REST que el entrenador exista. Roles permitidos: ADMIN, TRAINER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content),
            @ApiResponse(responseCode = "400", description = "El entrenador indicado no existe", content = @Content),
            @ApiResponse(responseCode = "503", description = "El microservicio de entrenadores no responde", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping
    public Clase programarClase(@RequestBody Clase clase) {
        return claseService.programarClase(clase);
    }

    @Operation(summary = "Cambiar el horario de una clase", description = "Actualiza el horario y publica el cambio por RabbitMQ (pub/sub) a miembros y entrenadores. Roles permitidos: ADMIN, TRAINER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horario actualizado y evento publicado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content),
            @ApiResponse(responseCode = "404", description = "La clase no existe", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PutMapping("/{id}/horario")
    public Clase actualizarHorario(@Parameter(description = "Id de la clase") @PathVariable Long id,
            @RequestBody CambioHorarioRequest request) {
        return claseService.actualizarHorario(id, request.horario());
    }

    @Operation(summary = "Listar clases", description = "Retorna todas las clases programadas. Roles permitidos: ADMIN, TRAINER, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public List<Clase> obtenerTodasLasClases() {
        return claseService.obtenerTodasLasClases();
    }

    @Operation(summary = "Obtener una clase", description = "Retorna la clase con el id indicado. Roles permitidos: ADMIN, TRAINER, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content),
            @ApiResponse(responseCode = "404", description = "La clase no existe", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/{id}")
    public Clase obtenerClasePorId(@Parameter(description = "Id de la clase") @PathVariable Long id) {
        return claseService.obtenerClasePorId(id);
    }
}
