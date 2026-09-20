package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.model.Entrenador;
import co.analisys.gimnasio.service.EntrenadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entrenador")
@Tag(name = "Entrenadores", description = "CRUD de los entrenadores del gimnasio")
public class EntrenadorController {

    @Autowired
    private EntrenadorService entrenadorService;

    @Operation(summary = "Crear un entrenador", description = "Registra un nuevo entrenador. Roles permitidos: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entrenador creado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Entrenador> agregarEntrenador(@RequestBody Entrenador entrenador) {
        Entrenador nuevo = entrenadorService.agregarEntrenador(entrenador);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @Operation(summary = "Listar entrenadores", description = "Retorna todos los entrenadores. Roles permitidos: ADMIN, TRAINER, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<List<Entrenador>> obtenerTodosEntrenadores() {
        return ResponseEntity.ok(entrenadorService.obtenerTodosEntrenadores());
    }

    @Operation(summary = "Obtener un entrenador", description = "Retorna el entrenador con el id indicado (lo consulta el microservicio de clases). Roles permitidos: ADMIN, TRAINER, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content),
            @ApiResponse(responseCode = "404", description = "El entrenador no existe", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<Entrenador> obtenerEntrenadorPorId(@Parameter(description = "Id del entrenador") @PathVariable Long id) {
        return entrenadorService.obtenerEntrenadorPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar un entrenador", description = "Actualiza los datos de un entrenador. Roles permitidos: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Entrenador> actualizarEntrenador(@Parameter(description = "Id del entrenador") @PathVariable Long id, @RequestBody Entrenador entrenador) {
        return ResponseEntity.ok(entrenadorService.actualizarEntrenador(id, entrenador));
    }

    @Operation(summary = "Eliminar un entrenador", description = "Elimina un entrenador. Roles permitidos: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Entrenador eliminado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEntrenador(@Parameter(description = "Id del entrenador") @PathVariable Long id) {
        entrenadorService.eliminarEntrenador(id);
        return ResponseEntity.noContent().build();
    }
}
