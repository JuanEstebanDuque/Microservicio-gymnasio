package co.analisys.miembros.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.analisys.miembros.dto.DatosEntrenamiento;
import co.analisys.miembros.dto.ResumenSemanal;
import co.analisys.miembros.service.EntrenamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/entrenamiento")
@Tag(name = "Entrenamiento (Kafka Streams)", description = "Datos de entrenamiento y resumen semanal por miembro")
public class EntrenamientoController {

    private final EntrenamientoService entrenamientoService;

    public EntrenamientoController(EntrenamientoService entrenamientoService) {
        this.entrenamientoService = entrenamientoService;
    }

    @Operation(summary = "Registrar datos de entrenamiento", description = "Publica la sesion en el topic datos-entrenamiento. Roles permitidos: ADMIN, TRAINER, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Datos publicados en Kafka"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void registrar(@RequestBody DatosEntrenamiento datos) {
        entrenamientoService.registrar(datos);
    }

    @Operation(summary = "Resumen semanal de un miembro", description = "Consulta el resultado del stream processor (ventana de 7 dias). Roles permitidos: ADMIN, TRAINER, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/resumen/{miembroId}")
    public List<ResumenSemanal> resumen(@PathVariable Long miembroId) {
        return entrenamientoService.resumenDelMiembro(miembroId);
    }
}
